package com.apollographql.federation.graphqljava;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;

import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

/**
 * https://www.apollographql.com/docs/federation/federation-spec/#type-_service
 *
 * _ServiceType 和 _serviceField
 */
final class _Service {
    static final String typeName = "_Service";
    static final String fieldName = "_service";
    static final String sdlFieldName = "sdl";

    static final GraphQLObjectType _ServiceType = newObject()
            .name(typeName)
            .field(newFieldDefinition()
                    .name(sdlFieldName)
                    .type(new GraphQLNonNull(Scalars.GraphQLString))
                    .build())
            .build();

    static final GraphQLFieldDefinition _serviceField = newFieldDefinition()
            .name(fieldName)
            .type(_ServiceType)
            .build();

    private _Service() {
    }
}
