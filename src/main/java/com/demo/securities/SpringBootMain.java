package com.demo.securities;

import com.demo.securities.config.DatabaseConfig;
import com.demo.securities.springboot.OptimisticLockDemoController;
import com.demo.securities.springfw.dao.TaiKhoanDao;
import com.demo.securities.springfw.service.OptimisticLockDemoService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import java.util.Properties;

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
 *
 * @Import 3 class JPA cho demo Optimistic Lock (TaiKhoanDao/OptimisticLockDemoService
 * tái dùng NGUYÊN từ SpringFrameworkMain, chỉ thêm 1 controller riêng) — vì các class
 * này không nằm trong "com.demo.securities.spring" nên component scan không tự thấy.
 * Khác hẳn SpringFrameworkMain (JpaConfig thủ công ~40 dòng: tự tạo DataSource,
 * LocalContainerEntityManagerFactoryBean, JpaTransactionManager...), ở đây spring-boot-
 * starter-data-jpa tự làm HẾT chỉ từ properties spring.datasource.* và spring.jpa.* set
 * bên dưới — đúng điểm so sánh "Hibernate/JPA" trong bảng ánh xạ ở README.
 */
@SpringBootApplication(scanBasePackages = "com.demo.securities.spring")
@Import({TaiKhoanDao.class, OptimisticLockDemoService.class, OptimisticLockDemoController.class})
public class SpringBootMain {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("server.port", String.valueOf(Integer.getInteger("server.port", 8087)));
        props.setProperty("spring.datasource.url", DatabaseConfig.getUrl());
        props.setProperty("spring.datasource.username", DatabaseConfig.getUser());
        props.setProperty("spring.datasource.password", DatabaseConfig.getPassword());
        props.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
        // Bang da co san (schema.sql o project chinh da tao) - khong de Hibernate tu sinh/sua DDL.
        props.setProperty("spring.jpa.hibernate.ddl-auto", "none");
        props.setProperty("spring.jpa.properties.hibernate.default_schema", DatabaseConfig.getSchema());
        props.setProperty("spring.jpa.open-in-view", "false");

        new SpringApplicationBuilder(SpringBootMain.class)
                .web(WebApplicationType.SERVLET)
                .properties(props)
                .run(args);
    }
}
