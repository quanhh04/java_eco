package com.demo.securities.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

@WebService
public interface TaiKhoanSoapService {

    @WebMethod
    TaiKhoanSoapDto moTaiKhoan(@WebParam(name = "khachHangId") String khachHangId,
                               @WebParam(name = "loaiTaiKhoan") String loaiTaiKhoan,
                               @WebParam(name = "soDuBanDau") double soDuBanDau) throws TaiKhoanFaultException;

    @WebMethod
    TaiKhoanSoapDto truyVanTaiKhoan(@WebParam(name = "soTaiKhoan") String soTaiKhoan) throws TaiKhoanFaultException;

    @WebMethod
    TaiKhoanSoapDto napTien(@WebParam(name = "soTaiKhoan") String soTaiKhoan,
                            @WebParam(name = "soTien") double soTien) throws TaiKhoanFaultException;

    @WebMethod
    TaiKhoanSoapDto rutTien(@WebParam(name = "soTaiKhoan") String soTaiKhoan,
                            @WebParam(name = "soTien") double soTien) throws TaiKhoanFaultException;

    @WebMethod
    TaiKhoanSoapDto khoaTaiKhoan(@WebParam(name = "soTaiKhoan") String soTaiKhoan) throws TaiKhoanFaultException;

    @WebMethod
    TaiKhoanSoapDto moKhoaTaiKhoan(@WebParam(name = "soTaiKhoan") String soTaiKhoan) throws TaiKhoanFaultException;

    @WebMethod
    TaiKhoanSoapDto dongTaiKhoan(@WebParam(name = "soTaiKhoan") String soTaiKhoan) throws TaiKhoanFaultException;
}
