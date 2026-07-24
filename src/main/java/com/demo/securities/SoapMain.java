package com.demo.securities;

import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.repository.impl.KhachHangRepositoryImpl;
import com.demo.securities.repository.impl.TaiKhoanRepositoryImpl;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.soap.TaiKhoanSoapServiceImpl;
import jakarta.xml.ws.Endpoint;

/**
 * Nhánh "Java EE / Jakarta EE - JAX-WS (SOAP)", implementation Apache CXF.
 * Publish qua API chuẩn Endpoint.publish() - CXF tự nhận (đăng ký qua
 * META-INF/services/jakarta.xml.ws.spi.Provider) và tự dựng embedded Jetty,
 * không cần Tomcat/servlet nào ở đây.
 */
public class SoapMain {

    public static void main(String[] args) {
        KhachHangRepository khachHangRepository = new KhachHangRepositoryImpl();
        TaiKhoanRepository taiKhoanRepository = new TaiKhoanRepositoryImpl();

        KhachHangService khachHangService = new KhachHangService(khachHangRepository, taiKhoanRepository);
        TaiKhoanService taiKhoanService = new TaiKhoanService(taiKhoanRepository, khachHangService);

        int port = Integer.getInteger("server.port", 8085);
        String address = "http://localhost:" + port + "/tai-khoan-soap";

        Endpoint.publish(address, new TaiKhoanSoapServiceImpl(taiKhoanService));

        System.out.println("SOAP service (JAX-WS/CXF) dang chay tai " + address);
        System.out.println("WSDL: " + address + "?wsdl");
    }
}
