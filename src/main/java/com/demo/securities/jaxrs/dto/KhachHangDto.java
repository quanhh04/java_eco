package com.demo.securities.jaxrs.dto;

public record KhachHangDto(
        String id,
        String hoTen,
        String ngaySinh,
        String gioiTinh,
        String soCCCD,
        String soDienThoai,
        String email,
        String diaChi,
        String ngayTao
) {
}
