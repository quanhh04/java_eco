package com.demo.securities.soap;

public class TaiKhoanFaultInfo {

    private String message;

    public TaiKhoanFaultInfo() {
    }

    public TaiKhoanFaultInfo(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
