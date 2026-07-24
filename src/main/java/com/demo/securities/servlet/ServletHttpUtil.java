package com.demo.securities.servlet;

import com.demo.securities.web.json.JsonWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ServletHttpUtil {

    private ServletHttpUtil() { }

    public static String readBody(HttpServletRequest request) throws IOException {
        return new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static void sendJson(HttpServletResponse response, int status, Object body) throws IOException {
        byte[] bytes = JsonWriter.write(body).getBytes(StandardCharsets.UTF_8);
        response.setStatus(status);
        response.setContentType("application/json; charset=utf-8");
        response.getOutputStream().write(bytes);
    }

    public static void sendNoContent(HttpServletResponse response, int status) {
        response.setStatus(status);
    }
}
