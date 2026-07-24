package com.demo.securities.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class KhachHang {

    private String id;
    private String hoTen;
    private LocalDate ngaySinh;
    private GioiTinh gioiTinh;
    private String soCCCD;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private LocalDateTime ngayTao;

    public KhachHang(String id, String hoTen, LocalDate ngaySinh, GioiTinh gioiTinh,
                      String soCCCD, String soDienThoai, String email, String diaChi,
                      LocalDateTime ngayTao) {
        this.id = id;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.soCCCD = soCCCD;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.diaChi = diaChi;
        this.ngayTao = ngayTao;
    }

    public String getId() { return id; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public GioiTinh getGioiTinh() { return gioiTinh; }
    public void setGioiTinh(GioiTinh gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getSoCCCD() { return soCCCD; }
    public void setSoCCCD(String soCCCD) { this.soCCCD = soCCCD; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public LocalDateTime getNgayTao() { return ngayTao; }

    @Override
    public String toString() {
        return String.format("%-8s %-25s %-12s %-6s %-14s %-12s %-25s %s",
                id, hoTen, ngaySinh, gioiTinh, soCCCD, soDienThoai, email, diaChi);
    }
}
