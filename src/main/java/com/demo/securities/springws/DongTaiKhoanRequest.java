package com.demo.securities.springws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DongTaiKhoanRequest", namespace = Namespaces.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class DongTaiKhoanRequest {

    private String soTaiKhoan;

    public String getSoTaiKhoan() {
        return soTaiKhoan;
    }

    public void setSoTaiKhoan(String soTaiKhoan) {
        this.soTaiKhoan = soTaiKhoan;
    }
}
