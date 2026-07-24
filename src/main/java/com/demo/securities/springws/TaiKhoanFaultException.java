package com.demo.securities.springws;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Dùng @SoapFault (cơ chế built-in của Spring-WS: faultcode + faultstring lấy
 * từ getMessage()) thay vì tự viết custom fault detail XML khớp phần tử
 * TaiKhoanFault trong XSD — giữ scope gọn. Khác JAX-WS (@WebFault + FaultInfo
 * bean tự viết tay đầy đủ), đây là idiom đơn giản hơn của Spring-WS cho lỗi
 * nghiệp vụ thông thường.
 */
@SoapFault(faultCode = FaultCode.SERVER)
public class TaiKhoanFaultException extends Exception {

    public TaiKhoanFaultException(String message) {
        super(message);
    }
}
