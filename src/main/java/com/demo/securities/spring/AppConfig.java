package com.demo.securities.spring;

import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.repository.impl.KhachHangRepositoryImpl;
import com.demo.securities.repository.impl.TaiKhoanRepositoryImpl;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public KhachHangRepository khachHangRepository() {
        return new KhachHangRepositoryImpl();
    }

    @Bean
    public TaiKhoanRepository taiKhoanRepository() {
        return new TaiKhoanRepositoryImpl();
    }

    @Bean
    public KhachHangService khachHangService(KhachHangRepository khachHangRepository,
                                              TaiKhoanRepository taiKhoanRepository) {
        return new KhachHangService(khachHangRepository, taiKhoanRepository);
    }

    @Bean
    public TaiKhoanService taiKhoanService(TaiKhoanRepository taiKhoanRepository,
                                            KhachHangService khachHangService) {
        return new TaiKhoanService(taiKhoanRepository, khachHangService);
    }
}
