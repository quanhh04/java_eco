package com.demo.securities.soap;

import jakarta.xml.ws.WebFault;

@WebFault(name = "TaiKhoanFault")
public class TaiKhoanFaultException extends Exception {

    private final TaiKhoanFaultInfo faultInfo;

    public TaiKhoanFaultException(String message, TaiKhoanFaultInfo faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public TaiKhoanFaultInfo getFaultInfo() {
        return faultInfo;
    }
}
