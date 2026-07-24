package com.demo.securities.jaxrs.dto;

public record TaiKhoanDto(
        String soTaiKhoan,
        String khachHangId,
        String loaiTaiKhoan,
        String trangThai,
        String ngayMo,
        double soDuTien
) {
}
