package com.demo.securities.model;

import java.time.LocalDateTime;

public class TaiKhoanChungKhoan {

    private String soTaiKhoan;
    private String khachHangId;
    private LoaiTaiKhoan loaiTaiKhoan;
    private TrangThaiTaiKhoan trangThai;
    private LocalDateTime ngayMo;
    private double soDuTien;

    public TaiKhoanChungKhoan(String soTaiKhoan, String khachHangId, LoaiTaiKhoan loaiTaiKhoan,
                               TrangThaiTaiKhoan trangThai, LocalDateTime ngayMo, double soDuTien) {
        this.soTaiKhoan = soTaiKhoan;
        this.khachHangId = khachHangId;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.trangThai = trangThai;
        this.ngayMo = ngayMo;
        this.soDuTien = soDuTien;
    }

    public String getSoTaiKhoan() { return soTaiKhoan; }

    public String getKhachHangId() { return khachHangId; }

    public LoaiTaiKhoan getLoaiTaiKhoan() { return loaiTaiKhoan; }

    public TrangThaiTaiKhoan getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiTaiKhoan trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayMo() { return ngayMo; }

    public double getSoDuTien() { return soDuTien; }
    public void setSoDuTien(double soDuTien) { this.soDuTien = soDuTien; }

    @Override
    public String toString() {
        return String.format("%-10s %-8s %-8s %-10s %-19s %,.0f",
                soTaiKhoan, khachHangId, loaiTaiKhoan, trangThai, ngayMo, soDuTien);
    }
}
