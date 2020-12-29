package com.apollographql.federation.graphqljava;

import graphql.PublicApi;
import graphql.language.DirectiveDefinition;
import graphql.language.DirectiveLocation;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.TypeName;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLNonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graphql.introspection.Introspection.DirectiveLocation.FIELD_DEFINITION;
import static graphql.introspection.Introspection.DirectiveLocation.INTERFACE;
import static graphql.introspection.Introspection.DirectiveLocation.OBJECT;
import static graphql.language.DirectiveDefinition.newDirectiveDefinition;
import static graphql.language.DirectiveLocation.newDirectiveLocation;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLDirective.newDirective;

@PublicApi
public final class FederationDirectives {
    /* Directive locations */

    private static final DirectiveLocation DL_OBJECT = newDirectiveLocation()
            .name("OBJECT")
            .build();

    private static final DirectiveLocation DL_INTERFACE = newDirectiveLocation()
            .name("INTERFACE")
            .build();

    private static final DirectiveLocation DL_FIELD_DEFINITION = newDirectiveLocation()
            .name("FIELD_DEFINITION")
            .build();

    /* fields: _FieldSet */

    private static final GraphQLArgument fieldsArgument = newArgument()
            .name("fields")
            // _FieldSet
            .type(new GraphQLNonNull(_FieldSet._FieldSetType))
            .build();

    private static GraphQLArgument fieldsArgument(String value) {
        return newArgument(fieldsArgument)
                .value(value)
                .build();
    }

    private static final InputValueDefinition fieldsDefinition = newInputValueDefinition()
            .name("fields")
            .type(new NonNullType(new TypeName(_FieldSet._FieldSet)))
            .build();

    /* directive @key(fields: _FieldSet!) on OBJECT | INTERFACE */

    public static final String keyName = "key";

    public static final GraphQLDirective key = newDirective()
            .name(keyName)
            .validLocations(OBJECT, INTERFACE)
            .argument(fieldsArgument)
            .build();

    public static GraphQLDirective key(String fields) {
        return newDirective(key)
                .argument(fieldsArgument(fields))
                .build();
    }

    public static final DirectiveDefinition keyDefinition = newDirectiveDefinition()
            .name(keyName)
            .directiveLocations(Arrays.asList(DL_OBJECT, DL_INTERFACE))
            .inputValueDefinition(fieldsDefinition)
            .build();

    /* directive @external on FIELD_DEFINITION */

    public static final String externalName = "external";

    public static final GraphQLDirective external = newDirective()
            .name(externalName)
            .validLocations(FIELD_DEFINITION)
            .build();

    public static final DirectiveDefinition externalDefinition = newDirectiveDefinition()
            .name(externalName)
            .directiveLocations(Collections.singletonList(DL_FIELD_DEFINITION))
            .build();

    /* directive @requires(fields: _FieldSet!) on FIELD_DEFINITION */

    public static final String requiresName = "requires";

    public static final GraphQLDirective requires = newDirective()
            .name(requiresName)
            .validLocations(FIELD_DEFINITION)
            .argument(fieldsArgument)
            .build();

    public static GraphQLDirective requires(String fields) {
        return newDirective(requires)
                .argument(fieldsArgument(fields))
                .build();
    }

    public static final DirectiveDefinition requiresDefinition = newDirectiveDefinition()
            .name(requiresName)
            .directiveLocations(Collections.singletonList(DL_FIELD_DEFINITION))
            .inputValueDefinition(fieldsDefinition)
            .build();

    /* directive @provides(fields: _FieldSet!) on FIELD_DEFINITION */

    public static final String providesName = "provides";

    public static final GraphQLDirective provides = newDirective()
            .name(providesName)
            .validLocations(FIELD_DEFINITION)
            .argument(fieldsArgument)
            .build();

    public static GraphQLDirective provides(String fields) {
        return newDirective(provides)
                .argument(fieldsArgument(fields))
                .build();
    }

    public static final DirectiveDefinition providesDefinition = newDirectiveDefinition()
            .name(providesName)
            .directiveLocations(Collections.singletonList(DL_FIELD_DEFINITION))
            .inputValueDefinition(fieldsDefinition)
            .build();

    /* directive @extends on OBJECT */

    public static final String extendsName = "extends";

    public static final GraphQLDirective extends_ = newDirective()
            .name(extendsName)
            .validLocations(OBJECT, INTERFACE)
            .build();

    public static final DirectiveDefinition extendsDefinition = newDirectiveDefinition()
            .name(extendsName)
            .directiveLocations(Arrays.asList(DL_OBJECT, DL_INTERFACE))
            .build();


    private FederationDirectives() {
    }

    /* Sets */

    public static final Set<String> allNames;
    public static final Set<GraphQLDirective> allDirectives;
    public static final Set<DirectiveDefinition> allDefinitions;

    static {
        // We need to maintain sorted order here for tests, since SchemaPrinter doesn't sort
        // directive definitions.
        allDirectives = Stream.of(
                key,
                external,
                requires,
                provides,
                extends_
        )
                .sorted(Comparator.comparing(GraphQLDirective::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        allDefinitions = Stream.of(
                keyDefinition,
                externalDefinition,
                requiresDefinition,
                providesDefinition,
                extendsDefinition
        )
                .sorted(Comparator.comparing(DirectiveDefinition::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        allNames = allDefinitions
                .stream()
                .map(DirectiveDefinition::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
