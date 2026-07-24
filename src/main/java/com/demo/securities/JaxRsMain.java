package com.demo.securities;

import com.demo.securities.jaxrs.SecuritiesApplication;
import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.repository.impl.KhachHangRepositoryImpl;
import com.demo.securities.repository.impl.TaiKhoanRepositoryImpl;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Nhánh "Java EE / Jakarta EE - JAX-RS (Jersey)": routing bằng annotation (@Path/@GET/@POST...),
 * JSON tự động qua Jackson, lỗi xử lý qua ExceptionMapper — framework lo phần mà ServletMain
 * phải tự viết tay. Cùng API shape với ServletMain/WebMain (đổi port là so sánh được ngay).
 */
public class JaxRsMain {

    public static void main(String[] args) throws Exception {
        KhachHangRepository khachHangRepository = new KhachHangRepositoryImpl();
        TaiKhoanRepository taiKhoanRepository = new TaiKhoanRepositoryImpl();

        KhachHangService khachHangService = new KhachHangService(khachHangRepository, taiKhoanRepository);
        TaiKhoanService taiKhoanService = new TaiKhoanService(taiKhoanRepository, khachHangService);

        int port = Integer.getInteger("server.port", 8084);

        Path baseDir = Files.createTempDirectory("jaxrs-tomcat");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", baseDir.toAbsolutePath().toString());

        ServletContainer jerseyServlet = new ServletContainer(
                new SecuritiesApplication(khachHangService, taiKhoanService));
        Tomcat.addServlet(ctx, "jerseyServlet", jerseyServlet);
        ctx.addServletMappingDecoded("/api/*", "jerseyServlet");

        tomcat.start();
        System.out.println("JAX-RS/Jersey (Jakarta EE) dang chay tai http://localhost:" + port);
        tomcat.getServer().await();
    }
}
