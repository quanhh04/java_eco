package com.demo.securities;

import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.repository.impl.KhachHangRepositoryImpl;
import com.demo.securities.repository.impl.TaiKhoanRepositoryImpl;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.servlet.KhachHangServlet;
import com.demo.securities.servlet.TaiKhoanServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Nhánh "Java EE / Jakarta EE - Servlet thuần": tự viết doGet/doPost/doPut/doDelete,
 * tự đọc/ghi JSON (tái dùng JsonWriter/JsonParser/RequestBody đã viết cho WebMain),
 * chạy trên Tomcat nhúng. So sánh trực tiếp với JaxRsMain (cùng API, khác cách viết).
 */
public class ServletMain {

    public static void main(String[] args) throws Exception {
        KhachHangRepository khachHangRepository = new KhachHangRepositoryImpl();
        TaiKhoanRepository taiKhoanRepository = new TaiKhoanRepositoryImpl();

        KhachHangService khachHangService = new KhachHangService(khachHangRepository, taiKhoanRepository);
        TaiKhoanService taiKhoanService = new TaiKhoanService(taiKhoanRepository, khachHangService);

        int port = Integer.getInteger("server.port", 8083);

        Path baseDir = Files.createTempDirectory("servlet-tomcat");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", baseDir.toAbsolutePath().toString());

        Tomcat.addServlet(ctx, "ping", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws java.io.IOException {
                resp.setContentType("text/plain; charset=utf-8");
                resp.getWriter().write("pong");
            }
        });
        ctx.addServletMappingDecoded("/ping", "ping");

        Tomcat.addServlet(ctx, "khachHangServlet", new KhachHangServlet(khachHangService));
        ctx.addServletMappingDecoded("/api/khach-hang/*", "khachHangServlet");

        Tomcat.addServlet(ctx, "taiKhoanServlet", new TaiKhoanServlet(taiKhoanService));
        ctx.addServletMappingDecoded("/api/tai-khoan/*", "taiKhoanServlet");

        tomcat.start();
        System.out.println("Servlet thuan (Jakarta EE) dang chay tai http://localhost:" + port);
        tomcat.getServer().await();
    }
}
