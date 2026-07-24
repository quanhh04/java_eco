package com.demo.securities.jaxrs.dto;

public record CreateKhachHangRequest(
        String hoTen,
        String ngaySinh,
        String gioiTinh,
        String soCCCD,
        String soDienThoai,
        String email,
        String diaChi
) {
}
