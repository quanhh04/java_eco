package com.demo.securities.springws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "KhoaTaiKhoanRequest", namespace = Namespaces.NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class KhoaTaiKhoanRequest {

    private String soTaiKhoan;

    public String getSoTaiKhoan() {
        return soTaiKhoan;
    }

    public void setSoTaiKhoan(String soTaiKhoan) {
        this.soTaiKhoan = soTaiKhoan;
    }
}
