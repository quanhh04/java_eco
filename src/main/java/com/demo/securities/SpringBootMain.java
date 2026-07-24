package com.demo.securities;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Nhánh "Spring ecosystem - Spring Boot (wrapper)": tái dùng NGUYÊN
 * KhachHangController/TaiKhoanController/GlobalExceptionHandler/AppConfig từ
 * package com.demo.securities.spring (giống hệt SpringMvcMain) — chỉ khác cách
 * khởi động. So với SpringMvcMain (tự dựng Tomcat + DispatcherServlet + context
 * bằng tay), ở đây không có dòng code hạ tầng nào cả: đây chính là "cái wrapper"
 * đỡ việc cho lập trình viên.
 *
 * scanBasePackages giới hạn đúng "com.demo.securities.spring" để KHÔNG bao giờ
 * quét trúng com.demo.securities.springmvc.WebMvcConfig (@EnableWebMvc) — nếu quét
 * trúng, Boot sẽ tắt auto-configuration MVC của chính nó.
 */
@SpringBootApplication(scanBasePackages = "com.demo.securities.spring")
public class SpringBootMain {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringBootMain.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=" + Integer.getInteger("server.port", 8087))
                .run(args);
    }
}
