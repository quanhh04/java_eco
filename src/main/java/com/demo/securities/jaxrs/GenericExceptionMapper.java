package com.demo.securities.jaxrs;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        exception.printStackTrace();
        return Response.status(500)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", "Loi he thong: " + exception.getMessage()))
                .build();
    }
}
