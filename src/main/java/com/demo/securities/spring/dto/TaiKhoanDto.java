package com.demo.securities.spring.dto;

public record TaiKhoanDto(
        String soTaiKhoan,
        String khachHangId,
        String loaiTaiKhoan,
        String trangThai,
        String ngayMo,
        double soDuTien
) {
}
