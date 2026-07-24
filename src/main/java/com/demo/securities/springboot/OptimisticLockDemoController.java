package com.demo.securities.springboot;

import com.demo.securities.springfw.service.OptimisticLockDemoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint riêng cho SpringBootMain, tái dùng NGUYÊN TaiKhoanDao/OptimisticLockDemoService
 * đã viết cho SpringFrameworkMain — khác biệt duy nhất là hạ tầng JPA phía sau: ở đây
 * KHÔNG có JpaConfig thủ công nào, toàn bộ DataSource/EntityManagerFactory/JpaTransactionManager
 * do spring-boot-starter-data-jpa tự cấu hình từ spring.datasource.* và spring.jpa.* properties
 * (xem SpringBootMain.main()) — đúng điểm so sánh trực tiếp với SpringFrameworkMain.
 */
@RestController
@RequestMapping("/api/tai-khoan")
public class OptimisticLockDemoController {

    private final OptimisticLockDemoService optimisticLockDemoService;

    public OptimisticLockDemoController(OptimisticLockDemoService optimisticLockDemoService) {
        this.optimisticLockDemoService = optimisticLockDemoService;
    }

    @PostMapping("/{so}/demo-optimistic-lock")
    public ResponseEntity<Map<String, String>> demoOptimisticLock(@PathVariable String so) {
        optimisticLockDemoService.demoXungDot(so, 100, 100);
        return ResponseEntity.ok(Map.of("ketQua", "Khong xung dot (khong nen xay ra voi demo nay)"));
    }
}
