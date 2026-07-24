package com.demo.securities.web.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonParser {

    private final String src;
    private int pos;

    private JsonParser(String src) {
        this.src = src;
        this.pos = 0;
    }

    public static Object parse(String json) {
        JsonParser parser = new JsonParser(json);
        parser.skipWhitespace();
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (parser.pos != parser.src.length()) {
            throw new JsonParseException("Ký tự thừa sau giá trị JSON tại vị trí " + parser.pos);
        }
        return value;
    }

    private Object parseValue() {
        char c = peek();
        return switch (c) {
            case '{' -> parseObject();
            case '[' -> parseArray();
            case '"' -> parseString();
            case 't', 'f' -> parseBoolean();
            case 'n' -> parseNull();
            default -> parseNumber();
        };
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return result;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw new JsonParseException("Kỳ vọng tên field dạng chuỗi tại vị trí " + pos);
            }
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            result.put(key, parseValue());
            skipWhitespace();
            char next = nextChar();
            if (next == '}') {
                break;
            }
            if (next != ',') {
                throw new JsonParseException("Kỳ vọng ',' hoặc '}' tại vị trí " + (pos - 1));
            }
        }
        return result;
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return result;
        }
        while (true) {
            skipWhitespace();
            result.add(parseValue());
            skipWhitespace();
            char next = nextChar();
            if (next == ']') {
                break;
            }
            if (next != ',') {
                throw new JsonParseException("Kỳ vọng ',' hoặc ']' tại vị trí " + (pos - 1));
            }
        }
        return result;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= src.length()) {
                throw new JsonParseException("Chuỗi không đóng tại vị trí " + pos);
            }
            char c = src.charAt(pos++);
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                if (pos >= src.length()) {
                    throw new JsonParseException("Escape không hợp lệ ở cuối chuỗi");
                }
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case 'r' -> sb.append('\r');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'u' -> sb.append(parseUnicodeEscape());
                    default -> throw new JsonParseException("Escape không hợp lệ: \\" + esc);
                }
            } else if (c < 0x20) {
                throw new JsonParseException("Ký tự điều khiển không hợp lệ trong chuỗi tại vị trí " + (pos - 1));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private char parseUnicodeEscape() {
        if (pos + 4 > src.length()) {
            throw new JsonParseException("Escape \\u không hợp lệ tại vị trí " + pos);
        }
        String hex = src.substring(pos, pos + 4);
        try {
            char c = (char) Integer.parseInt(hex, 16);
            pos += 4;
            return c;
        } catch (NumberFormatException e) {
            throw new JsonParseException("Escape \\u không hợp lệ: " + hex);
        }
    }

    private Boolean parseBoolean() {
        if (src.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        }
        if (src.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        throw new JsonParseException("Giá trị không hợp lệ tại vị trí " + pos);
    }

    private Object parseNull() {
        if (src.startsWith("null", pos)) {
            pos += 4;
            return null;
        }
        throw new JsonParseException("Giá trị không hợp lệ tại vị trí " + pos);
    }

    private Double parseNumber() {
        int start = pos;
        if (pos < src.length() && src.charAt(pos) == '-') {
            pos++;
        }
        if (pos >= src.length() || !Character.isDigit(src.charAt(pos))) {
            throw new JsonParseException("Số không hợp lệ tại vị trí " + start);
        }
        if (src.charAt(pos) == '0') {
            pos++;
        } else {
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                pos++;
            }
        }
        if (pos < src.length() && src.charAt(pos) == '.') {
            pos++;
            if (pos >= src.length() || !Character.isDigit(src.charAt(pos))) {
                throw new JsonParseException("Số không hợp lệ tại vị trí " + start);
            }
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                pos++;
            }
        }
        if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
            pos++;
            if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                pos++;
            }
            if (pos >= src.length() || !Character.isDigit(src.charAt(pos))) {
                throw new JsonParseException("Số không hợp lệ tại vị trí " + start);
            }
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                pos++;
            }
        }
        try {
            return Double.parseDouble(src.substring(start, pos));
        } catch (NumberFormatException e) {
            throw new JsonParseException("Số không hợp lệ tại vị trí " + start);
        }
    }

    private void skipWhitespace() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private char peek() {
        if (pos >= src.length()) {
            throw new JsonParseException("Thiếu dữ liệu JSON tại vị trí " + pos);
        }
        return src.charAt(pos);
    }

    private char nextChar() {
        if (pos >= src.length()) {
            throw new JsonParseException("Thiếu dữ liệu JSON tại vị trí " + pos);
        }
        return src.charAt(pos++);
    }

    private void expect(char expected) {
        char actual = nextChar();
        if (actual != expected) {
            throw new JsonParseException("Kỳ vọng '" + expected + "' nhưng gặp '" + actual + "' tại vị trí " + (pos - 1));
        }
    }
}
