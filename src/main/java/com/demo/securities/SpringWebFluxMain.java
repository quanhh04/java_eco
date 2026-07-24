package com.demo.securities;

import com.demo.securities.spring.AppConfig;
import com.demo.securities.spring.GlobalExceptionHandler;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

/**
 * Nhánh "Spring ecosystem - Spring WebFlux (Reactive)": scanBasePackages riêng
 * (com.demo.securities.springflux, KHÔNG scan com.demo.securities.spring) để tránh
 * đăng ký trùng route với KhachHangController/TaiKhoanController (bản blocking) —
 * chỉ @Import AppConfig (service/repo) và GlobalExceptionHandler (dùng lại được
 * nguyên vẹn vì @RestControllerAdvice hoạt động giống nhau ở cả MVC lẫn WebFlux).
 * Chạy trên Netty (mặc định của WebFlux), không phải Tomcat.
 */
@SpringBootApplication(scanBasePackages = "com.demo.securities.springflux")
@Import({AppConfig.class, GlobalExceptionHandler.class})
public class SpringWebFluxMain {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringWebFluxMain.class)
                .web(WebApplicationType.REACTIVE)
                .properties("server.port=" + Integer.getInteger("server.port", 8088))
                .run(args);
    }
}
