package com.demo.securities.graphql;

import com.demo.securities.exception.DuplicateException;
import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

/**
 * Idiom lỗi thứ 6: GraphQL không dùng HTTP status để báo lỗi nghiệp vụ (HTTP
 * response luôn là 200 OK) - lỗi được đưa vào mảng "errors" cùng cấp "data"
 * trong response JSON, kèm "extensions.classification" (BAD_REQUEST/NOT_FOUND).
 * @Component để Spring GraphQL tự phát hiện bean kiểu DataFetcherExceptionResolver.
 */
@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof NotFoundException) {
            return toError(ex, ErrorType.NOT_FOUND, env);
        }
        if (ex instanceof ValidationException || ex instanceof DuplicateException
                || ex instanceof IllegalArgumentException) {
            return toError(ex, ErrorType.BAD_REQUEST, env);
        }
        return toError(ex, ErrorType.INTERNAL_ERROR, env);
    }

    private GraphQLError toError(Throwable ex, ErrorType type, DataFetchingEnvironment env) {
        return GraphQLError.newError()
                .errorType(type)
                .message(ex.getMessage())
                .path(env.getExecutionStepInfo().getPath())
                .location(env.getField().getSourceLocation())
                .build();
    }
}
