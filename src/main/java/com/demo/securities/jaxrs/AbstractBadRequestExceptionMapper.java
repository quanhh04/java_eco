package com.demo.securities.jaxrs;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import java.util.Map;

/**
 * Base dùng chung cho các exception coi là lỗi input của client (400), tách theo
 * từng type cụ thể vì JAX-RS ExceptionMapper chỉ khớp theo 1 type generic duy nhất
 * (không gộp multi-catch được như try/catch thường) — mirror đúng danh sách exception
 * mà web/Router.java và servlet/BaseApiServlet.java đã map về 400.
 */
abstract class AbstractBadRequestExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    @Override
    public Response toResponse(T exception) {
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", String.valueOf(exception.getMessage())))
                .build();
    }
}
