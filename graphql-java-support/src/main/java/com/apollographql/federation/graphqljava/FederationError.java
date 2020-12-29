package com.apollographql.federation.graphqljava;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphQLException;
import graphql.language.SourceLocation;

import java.util.Collections;
import java.util.List;

public class FederationError extends GraphQLException implements GraphQLError {
    // 默认无效坐标
    private static final List<SourceLocation> NO_WHERE =
            Collections.singletonList(new SourceLocation(-1, -1));

    // 错误消息
    FederationError(String message) {
        super(message);
    }

    // 异常问题位置
    @Override
    public List<SourceLocation> getLocations() {
        return NO_WHERE;
    }

    // 异常分类
    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.ValidationError;
    }
}
