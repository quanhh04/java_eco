package com.demo.securities;

import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.repository.impl.KhachHangRepositoryImpl;
import com.demo.securities.repository.impl.TaiKhoanRepositoryImpl;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.web.HttpUtil;
import com.demo.securities.web.Router;
import com.demo.securities.web.WebServer;
import com.demo.securities.web.handler.KhachHangHandler;
import com.demo.securities.web.handler.TaiKhoanHandler;

import java.util.Map;

public class WebMain {

    public static void main(String[] args) throws Exception {
        KhachHangRepository khachHangRepository = new KhachHangRepositoryImpl();
        TaiKhoanRepository taiKhoanRepository = new TaiKhoanRepositoryImpl();

        KhachHangService khachHangService = new KhachHangService(khachHangRepository, taiKhoanRepository);
        TaiKhoanService taiKhoanService = new TaiKhoanService(taiKhoanRepository, khachHangService);

        int port = Integer.getInteger("server.port", 8080);

        Router router = new Router();
        router.get("/api/health", (exchange, pathParams) ->
                HttpUtil.sendJson(exchange, 200, Map.of("status", "ok")));
        new KhachHangHandler(khachHangService).register(router);
        new TaiKhoanHandler(taiKhoanService).register(router);

        WebServer server = new WebServer(port, router);
        server.start();
        System.out.println("Web service dang chay tai http://localhost:" + port);
    }
}
