package com.apollographql.federation.graphqljava;

import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;

/**
 * todo ？类型名称、字段名称和参数名称？
 */
public final class _Entity {
    public static final String REPRESENTATIONS = "representations";
    static final String _ENTITY = "_Entity";
    static final String _ENTITIES = "_entities";

    private _Entity() { }

    // graphql-java will mutate GraphQLTypeReference in-place,
    // so we need to create a new instance every time.
    static GraphQLFieldDefinition field(@NotNull Set<String> typeNames) {

        // _entities(representations: [_Any!]!): [_Entity]!
        return newFieldDefinition()
                .name(_ENTITIES)
                .argument(newArgument()
                        .name(REPRESENTATIONS)
                        .type(new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(new GraphQLTypeReference(_Any.typeName)))))
                        .build())
                .type(new GraphQLNonNull(
                                new GraphQLList(
                                        GraphQLUnionType.newUnionType()
                                                // fixme _Entity 是个 union，类型是 typeNames： union _Entity = typeName1 | typeNames2
                                                .name(_ENTITY)
                                                // 将 typeNames 转换成 相应名称的类型引用
                                                .possibleTypes(typeNames.stream()
                                                        .map(GraphQLTypeReference::new)
                                                        .toArray(GraphQLTypeReference[]::new))
                                                .build()
                                )
                        )
                )
                .build();
    }
}
