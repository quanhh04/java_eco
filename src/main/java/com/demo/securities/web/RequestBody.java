package com.demo.securities.web;

import com.demo.securities.exception.ValidationException;
import com.demo.securities.web.json.JsonParseException;
import com.demo.securities.web.json.JsonParser;

import java.util.Map;

public final class RequestBody {

    private RequestBody() { }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object value = JsonParser.parse(json);
        if (!(value instanceof Map)) {
            throw new JsonParseException("Yêu cầu body phải là một JSON object");
        }
        return (Map<String, Object>) value;
    }

    public static String requireString(Map<String, Object> body, String field) {
        Object value = body.get(field);
        if (!(value instanceof String s) || s.isBlank()) {
            throw new ValidationException("Thiếu field bắt buộc: " + field);
        }
        return s;
    }

    public static double requireDouble(Map<String, Object> body, String field) {
        Object value = body.get(field);
        if (!(value instanceof Number n)) {
            throw new ValidationException("Thiếu hoặc sai kiểu số cho field: " + field);
        }
        return n.doubleValue();
    }

    public static double optionalDouble(Map<String, Object> body, String field, double defaultValue) {
        Object value = body.get(field);
        if (value == null) {
            return defaultValue;
        }
        if (!(value instanceof Number n)) {
            throw new ValidationException("Sai kiểu số cho field: " + field);
        }
        return n.doubleValue();
    }
}
