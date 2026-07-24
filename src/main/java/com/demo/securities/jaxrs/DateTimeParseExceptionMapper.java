package com.demo.securities.jaxrs;

import jakarta.ws.rs.ext.Provider;

import java.time.format.DateTimeParseException;

@Provider
public class DateTimeParseExceptionMapper extends AbstractBadRequestExceptionMapper<DateTimeParseException> {
}
