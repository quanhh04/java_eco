package com.demo.securities.jaxrs;

import com.demo.securities.exception.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException exception) {
        return Response.status(404)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", exception.getMessage()))
                .build();
    }
}
