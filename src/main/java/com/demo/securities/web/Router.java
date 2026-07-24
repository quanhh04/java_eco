package com.demo.securities.web;

import com.demo.securities.exception.DuplicateException;
import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import com.demo.securities.web.json.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Router implements HttpHandler {

    private record Route(String method, List<String> segments, RouteHandler handler) { }

    private final List<Route> routes = new ArrayList<>();

    public void get(String pattern, RouteHandler handler) {
        addRoute("GET", pattern, handler);
    }

    public void post(String pattern, RouteHandler handler) {
        addRoute("POST", pattern, handler);
    }

    public void put(String pattern, RouteHandler handler) {
        addRoute("PUT", pattern, handler);
    }

    public void delete(String pattern, RouteHandler handler) {
        addRoute("DELETE", pattern, handler);
    }

    private void addRoute(String method, String pattern, RouteHandler handler) {
        routes.add(new Route(method, splitPath(pattern), handler));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            dispatch(exchange);
        } finally {
            try {
                exchange.getRequestBody().readAllBytes();
            } catch (IOException ignored) {
                // body có thể đã được handler đọc và luồng đã đóng, bỏ qua khi dọn dẹp
            }
            exchange.close();
        }
    }

    private void dispatch(HttpExchange exchange) throws IOException {
        String path = normalizePath(exchange.getRequestURI().getPath());
        List<String> pathSegments = splitPath(path);
        String httpMethod = exchange.getRequestMethod();

        Set<String> allowedMethods = new LinkedHashSet<>();
        for (Route route : routes) {
            Map<String, String> pathParams = matchSegments(route.segments(), pathSegments);
            if (pathParams == null) {
                continue;
            }
            allowedMethods.add(route.method());
            if (!route.method().equals(httpMethod)) {
                continue;
            }
            invoke(exchange, route.handler(), pathParams);
            return;
        }

        if (allowedMethods.isEmpty()) {
            HttpUtil.sendJson(exchange, 404, Map.of("error", "Không tìm thấy đường dẫn: " + path));
        } else {
            exchange.getResponseHeaders().set("Allow", String.join(", ", allowedMethods));
            HttpUtil.sendJson(exchange, 405, Map.of("error", "Method không được hỗ trợ cho đường dẫn này"));
        }
    }

    private void invoke(HttpExchange exchange, RouteHandler handler, Map<String, String> pathParams) throws IOException {
        try {
            handler.handle(exchange, pathParams);
        } catch (NotFoundException e) {
            HttpUtil.sendJson(exchange, 404, Map.of("error", e.getMessage()));
        } catch (ValidationException | DuplicateException | JsonParseException | IllegalArgumentException
                 | DateTimeParseException | ClassCastException | NullPointerException e) {
            HttpUtil.sendJson(exchange, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

    private Map<String, String> matchSegments(List<String> patternSegments, List<String> pathSegments) {
        if (patternSegments.size() != pathSegments.size()) {
            return null;
        }
        Map<String, String> pathParams = new LinkedHashMap<>();
        for (int i = 0; i < patternSegments.size(); i++) {
            String patternSegment = patternSegments.get(i);
            String pathSegment = pathSegments.get(i);
            if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                pathParams.put(patternSegment.substring(1, patternSegment.length() - 1), pathSegment);
            } else if (!patternSegment.equals(pathSegment)) {
                return null;
            }
        }
        return pathParams;
    }

    private static String normalizePath(String path) {
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static List<String> splitPath(String path) {
        List<String> segments = new ArrayList<>();
        for (String segment : path.split("/")) {
            if (!segment.isBlank()) {
                segments.add(segment);
            }
        }
        return segments;
    }
}
