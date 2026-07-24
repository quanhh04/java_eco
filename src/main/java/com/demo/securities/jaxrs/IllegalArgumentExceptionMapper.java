package com.demo.securities.jaxrs;

import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper extends AbstractBadRequestExceptionMapper<IllegalArgumentException> {
}
