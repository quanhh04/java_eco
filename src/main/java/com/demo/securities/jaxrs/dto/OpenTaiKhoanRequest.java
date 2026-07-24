package com.demo.securities.jaxrs.dto;

public record OpenTaiKhoanRequest(
        String khachHangId,
        String loaiTaiKhoan,
        Double soDuBanDau
) {
}
