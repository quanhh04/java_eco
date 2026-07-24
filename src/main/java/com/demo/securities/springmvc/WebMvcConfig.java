package com.demo.securities.springmvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Nằm ở package RIÊNG (springmvc), không phải com.demo.securities.spring —
 * để SpringBootMain (component-scan com.demo.securities.spring) KHÔNG bao giờ
 * quét trúng class này. Nếu Boot vô tình thấy @EnableWebMvc, nó sẽ tắt luôn
 * auto-configuration MVC của mình (nhường hoàn toàn cho cấu hình thủ công),
 * làm mất ý nghĩa so sánh "Boot đỡ code tới đâu" so với SpringMvcMain.
 */
@Configuration
@EnableWebMvc
public class WebMvcConfig {
}
