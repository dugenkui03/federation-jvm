package com.apollographql.federation.graphqljava;

import graphql.Scalars;
import graphql.language.ScalarTypeDefinition;
import graphql.schema.GraphQLScalarType;

/**
 * 标量：代表一个 field 集合
 *
 * todo 看一下这个字段绑定的哪个DF
 */
public final class _FieldSet {
    static final String _FieldSet = "_FieldSet";

    public static GraphQLScalarType _FieldSetType = GraphQLScalarType.newScalar(Scalars.GraphQLString)
            .name(_FieldSet)
            .description(null)
            .coercing(Scalars.GraphQLString.getCoercing())
            .build();

    public static final ScalarTypeDefinition definition = ScalarTypeDefinition.newScalarTypeDefinition()
            .name(_FieldSet)
            .build();
}
