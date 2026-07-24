package com.demo.securities.soap;

public class TaiKhoanSoapDto {

    private String soTaiKhoan;
    private String khachHangId;
    private String loaiTaiKhoan;
    private String trangThai;
    private String ngayMo;
    private double soDuTien;

    public TaiKhoanSoapDto() {
    }

    public TaiKhoanSoapDto(String soTaiKhoan, String khachHangId, String loaiTaiKhoan,
                           String trangThai, String ngayMo, double soDuTien) {
        this.soTaiKhoan = soTaiKhoan;
        this.khachHangId = khachHangId;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.trangThai = trangThai;
        this.ngayMo = ngayMo;
        this.soDuTien = soDuTien;
    }

    public String getSoTaiKhoan() {
        return soTaiKhoan;
    }

    public void setSoTaiKhoan(String soTaiKhoan) {
        this.soTaiKhoan = soTaiKhoan;
    }

    public String getKhachHangId() {
        return khachHangId;
    }

    public void setKhachHangId(String khachHangId) {
        this.khachHangId = khachHangId;
    }

    public String getLoaiTaiKhoan() {
        return loaiTaiKhoan;
    }

    public void setLoaiTaiKhoan(String loaiTaiKhoan) {
        this.loaiTaiKhoan = loaiTaiKhoan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getNgayMo() {
        return ngayMo;
    }

    public void setNgayMo(String ngayMo) {
        this.ngayMo = ngayMo;
    }

    public double getSoDuTien() {
        return soDuTien;
    }

    public void setSoDuTien(double soDuTien) {
        this.soDuTien = soDuTien;
    }
}
