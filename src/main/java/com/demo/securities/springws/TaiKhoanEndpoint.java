package com.demo.securities.springws;

import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Dispatch theo @PayloadRoot (namespace + tên phần tử gốc của request payload) —
 * khác hẳn @Path/@GET của JAX-RS hay @RequestMapping của Spring MVC: Spring-WS
 * route dựa trên NỘI DUNG XML, không dựa trên URL/method.
 */
@Endpoint
public class TaiKhoanEndpoint {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanEndpoint(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @PayloadRoot(namespace = Namespaces.NS, localPart = "MoTaiKhoanRequest")
    @ResponsePayload
    public TaiKhoanResponse moTaiKhoan(@RequestPayload MoTaiKhoanRequest request) throws TaiKhoanFaultException {
        return wrap(() -> toResponse(taiKhoanService.moTaiKhoan(
                request.getKhachHangId(),
                LoaiTaiKhoan.valueOf(request.getLoaiTaiKhoan().toUpperCase()),
                request.getSoDuBanDau())));
    }

    @PayloadRoot(namespace = Namespaces.NS, localPart = "TruyVanTaiKhoanRequest")
    @ResponsePayload
    public TaiKhoanResponse truyVanTaiKhoan(@RequestPayload TruyVanTaiKhoanRequest request) throws TaiKhoanFaultException {
        return wrap(() -> toResponse(taiKhoanService.timTheoSo(request.getSoTaiKhoan())));
    }

    @PayloadRoot(namespace = Namespaces.NS, localPart = "NapTienRequest")
    @ResponsePayload
    public TaiKhoanResponse napTien(@RequestPayload NapTienRequest request) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.napTien(request.getSoTaiKhoan(), request.getSoTien());
            return toResponse(taiKhoanService.timTheoSo(request.getSoTaiKhoan()));
        });
    }

    @PayloadRoot(namespace = Namespaces.NS, localPart = "RutTienRequest")
    @ResponsePayload
    public TaiKhoanResponse rutTien(@RequestPayload RutTienRequest request) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.rutTien(request.getSoTaiKhoan(), request.getSoTien());
            return toResponse(taiKhoanService.timTheoSo(request.getSoTaiKhoan()));
        });
    }

    @PayloadRoot(namespace = Namespaces.NS, localPart = "KhoaTaiKhoanRequest")
    @ResponsePayload
    public TaiKhoanResponse khoaTaiKhoan(@RequestPayload KhoaTaiKhoanRequest request) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.khoaTaiKhoan(request.getSoTaiKhoan());
            return toResponse(taiKhoanService.timTheoSo(request.getSoTaiKhoan()));
        });
    }

    @PayloadRoot(namespace = Namespaces.NS, localPart = "MoKhoaTaiKhoanRequest")
    @ResponsePayload
    public TaiKhoanResponse moKhoaTaiKhoan(@RequestPayload MoKhoaTaiKhoanRequest request) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.moKhoaTaiKhoan(request.getSoTaiKhoan());
            return toResponse(taiKhoanService.timTheoSo(request.getSoTaiKhoan()));
        });
    }

    @PayloadRoot(namespace = Namespaces.NS, localPart = "DongTaiKhoanRequest")
    @ResponsePayload
    public TaiKhoanResponse dongTaiKhoan(@RequestPayload DongTaiKhoanRequest request) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.dongTaiKhoan(request.getSoTaiKhoan());
            return toResponse(taiKhoanService.timTheoSo(request.getSoTaiKhoan()));
        });
    }

    private interface WsAction {
        TaiKhoanResponse run();
    }

    private TaiKhoanResponse wrap(WsAction action) throws TaiKhoanFaultException {
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw new TaiKhoanFaultException(e.getMessage());
        }
    }

    private static TaiKhoanResponse toResponse(TaiKhoanChungKhoan taiKhoan) {
        return new TaiKhoanResponse(
                taiKhoan.getSoTaiKhoan(),
                taiKhoan.getKhachHangId(),
                taiKhoan.getLoaiTaiKhoan().name(),
                taiKhoan.getTrangThai().name(),
                taiKhoan.getNgayMo().toString(),
                taiKhoan.getSoDuTien());
    }
}
