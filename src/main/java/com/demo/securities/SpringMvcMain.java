package com.demo.securities;

import com.demo.securities.spring.AppConfig;
import com.demo.securities.spring.GlobalExceptionHandler;
import com.demo.securities.spring.KhachHangController;
import com.demo.securities.spring.TaiKhoanController;
import com.demo.securities.springmvc.WebMvcConfig;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Nhánh "Spring ecosystem - Spring MVC (không Spring Boot)": tự dựng
 * AnnotationConfigWebApplicationContext + DispatcherServlet + Tomcat nhúng bằng tay,
 * y hệt pattern ServletMain/JaxRsMain, chỉ khác "servlet" giờ là của Spring.
 * So sánh trực tiếp với SpringBootMain (cùng controller, khác cách khởi động)
 * để thấy rõ Spring Boot đỡ code tới đâu.
 */
public class SpringMvcMain {

    public static void main(String[] args) throws Exception {
        int port = Integer.getInteger("server.port", 8086);

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class, WebMvcConfig.class,
                KhachHangController.class, TaiKhoanController.class, GlobalExceptionHandler.class);

        Path baseDir = Files.createTempDirectory("springmvc-tomcat");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", baseDir.toAbsolutePath().toString());

        Tomcat.addServlet(ctx, "dispatcherServlet", new DispatcherServlet(context));
        ctx.addServletMappingDecoded("/*", "dispatcherServlet");

        tomcat.start();
        System.out.println("Spring MVC (khong Spring Boot) dang chay tai http://localhost:" + port);
        tomcat.getServer().await();
    }
}
