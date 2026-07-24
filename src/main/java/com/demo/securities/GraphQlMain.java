package com.demo.securities;

import com.demo.securities.spring.AppConfig;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

/**
 * Nhánh "Giao thức khác - GraphQL": dùng Spring GraphQL (spring-boot-starter-graphql),
 * tái dùng hạ tầng Spring Boot đã ổn định thay vì tự viết HTTP handler tay lần nữa
 * (khác gRPC/WebSocket ở trên - 2 cái đó tự dựng server tay vì không có sẵn tầng
 * Spring cho giao thức nhị phân/2 chiều). Toàn bộ request đi qua 1 endpoint HTTP
 * POST /graphql duy nhất - schema.graphqls định nghĩa hợp đồng, không phải @RequestMapping.
 *
 * scanBasePackages giới hạn "com.demo.securities.graphql" để không quét trúng
 * KhachHangController/TaiKhoanController REST ở package "spring" (dùng chung AppConfig
 * qua @Import nhưng không cần Controller REST đó).
 */
@SpringBootApplication(scanBasePackages = "com.demo.securities.graphql")
@Import(AppConfig.class)
public class GraphQlMain {

    public static void main(String[] args) {
        new SpringApplicationBuilder(GraphQlMain.class)
                .web(WebApplicationType.SERVLET)
                .properties("server.port=" + Integer.getInteger("server.port", 8092))
                .run(args);
    }
}
