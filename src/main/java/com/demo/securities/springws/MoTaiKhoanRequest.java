package com.demo.securities.springws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MoTaiKhoanRequest", namespace = Namespaces.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class MoTaiKhoanRequest {

    private String khachHangId;
    private String loaiTaiKhoan;
    private double soDuBanDau;

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

    public double getSoDuBanDau() {
        return soDuBanDau;
    }

    public void setSoDuBanDau(double soDuBanDau) {
        this.soDuBanDau = soDuBanDau;
    }
}
