package com.demo.securities.web;

import com.sun.net.httpserver.HttpExchange;

import java.util.Map;

@FunctionalInterface
public interface RouteHandler {
    void handle(HttpExchange exchange, Map<String, String> pathParams) throws Exception;
}
