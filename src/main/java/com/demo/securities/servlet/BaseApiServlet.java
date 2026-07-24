package com.demo.securities.servlet;

import com.demo.securities.exception.DuplicateException;
import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import com.demo.securities.web.json.JsonParseException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.Map;

public abstract class BaseApiServlet extends HttpServlet {

    protected interface ServletAction {
        void run() throws Exception;
    }

    protected void handle(HttpServletResponse response, ServletAction action) throws IOException {
        try {
            action.run();
        } catch (NotFoundException e) {
            ServletHttpUtil.sendJson(response, 404, Map.of("error", e.getMessage()));
        } catch (ValidationException | DuplicateException | JsonParseException | IllegalArgumentException
                 | DateTimeParseException | ClassCastException | NullPointerException e) {
            ServletHttpUtil.sendJson(response, 400, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ServletHttpUtil.sendJson(response, 500, Map.of("error", "Loi he thong: " + e.getMessage()));
        }
    }
}
