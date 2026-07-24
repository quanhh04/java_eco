package com.demo.securities.spring.dto;

public record OpenTaiKhoanRequest(
        String khachHangId,
        String loaiTaiKhoan,
        Double soDuBanDau
) {
}
