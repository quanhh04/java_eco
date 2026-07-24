package com.demo.securities.jaxrs;

import jakarta.ws.rs.ext.Provider;

@Provider
public class ClassCastExceptionMapper extends AbstractBadRequestExceptionMapper<ClassCastException> {
}
