package com.apollographql.federation.graphqljava;

import graphql.GraphQLError;
import graphql.schema.Coercing;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetcherFactories;
import graphql.schema.DataFetcherFactory;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirectiveContainer;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.TypeResolver;
import graphql.schema.idl.errors.SchemaProblem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SchemaTransformer {

    // 标记节点
    private static final Object DUMMY = new Object();

    // Apollo Gateway will fail composition
    // if it sees standard directive definitions.
    private static final Set<String> STANDARD_DIRECTIVES = new HashSet<>(
            Arrays.asList("deprecated", "include", "skip", "specifiedBy")
    );

    /**
     * 原始未修改的schema
     */
    private final GraphQLSchema originalSchema;

    /**
     * 类型解析器
     */
    private TypeResolver entityTypeResolver = null;

    /**
     * 实体DF
     */
    private DataFetcher entitiesDataFetcher = null;
    private DataFetcherFactory entitiesDataFetcherFactory = null;

    /**
     * _Any的转换器
     */
    private Coercing coercingForAny = _Any.defaultCoercing;

    SchemaTransformer(GraphQLSchema originalSchema) {
        this.originalSchema = originalSchema;
    }

    @NotNull
    public SchemaTransformer resolveEntityType(TypeResolver entityTypeResolver) {
        this.entityTypeResolver = entityTypeResolver;
        return this;
    }


    /**
     * todo DF 和 DFFactory 不一起设置吗？
     */
    @NotNull
    public SchemaTransformer fetchEntities(DataFetcher entitiesDataFetcher) {
        this.entitiesDataFetcher = entitiesDataFetcher;
        this.entitiesDataFetcherFactory = null;
        return this;
    }

    @NotNull
    public SchemaTransformer fetchEntitiesFactory(DataFetcherFactory entitiesDataFetcherFactory) {
        this.entitiesDataFetcher = null;
        this.entitiesDataFetcherFactory = entitiesDataFetcherFactory;
        return this;
    }

    public SchemaTransformer coercingForAny(Coercing coercing) {
        this.coercingForAny = coercing;
        return this;
    }

    @NotNull
    public final GraphQLSchema build() throws SchemaProblem {

        // 创建一个新的schema
        final GraphQLSchema.Builder newSchema = GraphQLSchema.newSchema(originalSchema);

        // 创建一个新的GraphQLCodeRegistry
        final GraphQLCodeRegistry.Builder newCodeRegistry =
                GraphQLCodeRegistry.newCodeRegistry(originalSchema.getCodeRegistry());

        // 获取查询类型
        final GraphQLObjectType originalQueryType = originalSchema.getQueryType();

        // 新的查询对象：fixme 添加了 类型为 _Service 的字段 _service
        final GraphQLObjectType.Builder newQueryType = GraphQLObjectType.newObject(originalQueryType)
                .field(_Service._serviceField);

        // 获取 Query._service 字段坐标
        FieldCoordinates _serviceCoordinates = FieldCoordinates.coordinates(
                originalQueryType.getName(),
                _Service.fieldName // _service
        );
        // todo _serviceCoordinates 返回 DUMMY 对象
        newCodeRegistry.dataFetcher(_serviceCoordinates, (DataFetcher<Object>) environment -> DUMMY);

        // _Service.sdl 字段坐标
        FieldCoordinates _ServiceSdl = FieldCoordinates.coordinates(
                _Service.typeName,
                _Service.sdlFieldName
        );
        // todo 返回的是什么
        // Print the original schema as sdl and expose it as query { _service { sdl } }
        final String sdl = sdl(originalSchema);
        newCodeRegistry.dataFetcher(_ServiceSdl, (DataFetcher<String>) environment -> sdl);

        // fixme 获取所有包含 @key 的类型名称
        // Collecting all entity types: Types with @key directive and all types that implement them
        final Set<String> entityTypeNames = originalSchema.getAllTypesAsList().stream()
                .filter(t -> t instanceof GraphQLDirectiveContainer &&
                        ((GraphQLDirectiveContainer) t).getDirective(FederationDirectives.keyName) != null)
                .map(GraphQLNamedType::getName)
                .collect(Collectors.toSet());

        // todo
        final Set<String> entityConcreteTypeNames = originalSchema.getAllTypesAsList()
                .stream()
                .filter(type -> type instanceof GraphQLObjectType)
                .filter(type -> entityTypeNames.contains(type.getName()) ||
                        // todo 或者其继承的接口包括 @key指令？
                        ((GraphQLObjectType) type).getInterfaces()
                                .stream()
                                .anyMatch(itf -> entityTypeNames.contains(itf.getName())))
                .map(GraphQLNamedType::getName)
                .collect(Collectors.toSet());


        final List<GraphQLError> errors = new ArrayList<>();
        // If there are entity types install: Query._entities(representations: [_Any!]!): [_Entity]!
        if (!entityConcreteTypeNames.isEmpty()) {
            newQueryType.field(_Entity.field(entityConcreteTypeNames));

            final GraphQLType originalAnyType = originalSchema.getType(_Any.typeName);
            if (originalAnyType == null) {
                newSchema.additionalType(_Any.type(coercingForAny));
            }

            if (entityTypeResolver != null) {
                newCodeRegistry.typeResolver(_Entity.typeName, entityTypeResolver);
            } else {
                if (!newCodeRegistry.hasTypeResolver(_Entity.typeName)) {
                    errors.add(new FederationError("Missing a type resolver for _Entity"));
                }
            }

            final FieldCoordinates _entities = FieldCoordinates.coordinates(originalQueryType.getName(), _Entity.fieldName);
            if (entitiesDataFetcher != null) {
                newCodeRegistry.dataFetcher(_entities, entitiesDataFetcher);
            } else if (entitiesDataFetcherFactory != null) {
                newCodeRegistry.dataFetcher(_entities, entitiesDataFetcherFactory);
            } else if (!newCodeRegistry.hasDataFetcher(_entities)) {
                errors.add(new FederationError("Missing a data fetcher for _entities"));
            }
        }

        if (!errors.isEmpty()) {
            throw new SchemaProblem(errors);
        }

        return newSchema
                .query(newQueryType.build())
                .codeRegistry(newCodeRegistry.build())
                .build();
    }

    public static String sdl(GraphQLSchema schema) {
        // Gather directive definitions to hide.
        final Set<String> hiddenDirectiveDefinitions = new HashSet<>();
        hiddenDirectiveDefinitions.addAll(STANDARD_DIRECTIVES);
        hiddenDirectiveDefinitions.addAll(FederationDirectives.allNames);

        // Gather type definitions to hide.
        final Set<String> hiddenTypeDefinitions = new HashSet<>();
        hiddenTypeDefinitions.add(_Any.typeName);
        hiddenTypeDefinitions.add(_Entity.typeName);
        hiddenTypeDefinitions.add(_FieldSet.typeName);
        hiddenTypeDefinitions.add(_Service.typeName);

        // Note that FederationSdlPrinter is a copy of graphql-java's SchemaPrinter that adds the
        // ability to filter out directive and type definitions, which is required by federation
        // spec.
        //
        // FederationSdlPrinter will need to be updated whenever graphql-java changes versions. It
        // can be removed when graphql-java adds native support for filtering out directive and
        // type definitions or federation spec changes to allow the currently forbidden directive
        // and type definitions.
        final FederationSdlPrinter.Options options = FederationSdlPrinter.Options.defaultOptions()
                .includeScalarTypes(true)
                .includeSchemaDefinition(true)
                .includeDirectives(true)
                .includeDirectiveDefinitions(def -> !hiddenDirectiveDefinitions.contains(def.getName()))
                .includeTypeDefinitions(def -> !hiddenTypeDefinitions.contains(def.getName()));
        return new FederationSdlPrinter(options).print(schema);
    }
}
