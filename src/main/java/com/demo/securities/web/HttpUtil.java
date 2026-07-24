package com.demo.securities.web;

import com.demo.securities.web.json.JsonWriter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HttpUtil {

    private HttpUtil() { }

    public static String readBody(HttpExchange exchange) throws IOException {
        InputStream in = exchange.getRequestBody();
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    public static void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JsonWriter.write(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    public static void sendNoContent(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
    }

    public static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> params = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return params;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.isBlank()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            params.put(
                    URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8));
        }
        return params;
    }
}
