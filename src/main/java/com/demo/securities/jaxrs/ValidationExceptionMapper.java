package com.demo.securities.jaxrs;

import com.demo.securities.exception.ValidationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException exception) {
        return Response.status(400)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", exception.getMessage()))
                .build();
    }
}
