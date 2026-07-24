package com.demo.securities.soap;

import com.demo.securities.exception.DataAccessException;
import com.demo.securities.exception.DuplicateException;
import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import jakarta.jws.WebService;

@WebService(endpointInterface = "com.demo.securities.soap.TaiKhoanSoapService")
public class TaiKhoanSoapServiceImpl implements TaiKhoanSoapService {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanSoapServiceImpl(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @Override
    public TaiKhoanSoapDto moTaiKhoan(String khachHangId, String loaiTaiKhoan, double soDuBanDau) throws TaiKhoanFaultException {
        return wrap(() -> toDto(taiKhoanService.moTaiKhoan(
                khachHangId, LoaiTaiKhoan.valueOf(loaiTaiKhoan.toUpperCase()), soDuBanDau)));
    }

    @Override
    public TaiKhoanSoapDto truyVanTaiKhoan(String soTaiKhoan) throws TaiKhoanFaultException {
        return wrap(() -> toDto(taiKhoanService.timTheoSo(soTaiKhoan)));
    }

    @Override
    public TaiKhoanSoapDto napTien(String soTaiKhoan, double soTien) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.napTien(soTaiKhoan, soTien);
            return toDto(taiKhoanService.timTheoSo(soTaiKhoan));
        });
    }

    @Override
    public TaiKhoanSoapDto rutTien(String soTaiKhoan, double soTien) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.rutTien(soTaiKhoan, soTien);
            return toDto(taiKhoanService.timTheoSo(soTaiKhoan));
        });
    }

    @Override
    public TaiKhoanSoapDto khoaTaiKhoan(String soTaiKhoan) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.khoaTaiKhoan(soTaiKhoan);
            return toDto(taiKhoanService.timTheoSo(soTaiKhoan));
        });
    }

    @Override
    public TaiKhoanSoapDto moKhoaTaiKhoan(String soTaiKhoan) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.moKhoaTaiKhoan(soTaiKhoan);
            return toDto(taiKhoanService.timTheoSo(soTaiKhoan));
        });
    }

    @Override
    public TaiKhoanSoapDto dongTaiKhoan(String soTaiKhoan) throws TaiKhoanFaultException {
        return wrap(() -> {
            taiKhoanService.dongTaiKhoan(soTaiKhoan);
            return toDto(taiKhoanService.timTheoSo(soTaiKhoan));
        });
    }

    private interface SoapAction {
        TaiKhoanSoapDto run();
    }

    private TaiKhoanSoapDto wrap(SoapAction action) throws TaiKhoanFaultException {
        try {
            return action.run();
        } catch (ValidationException | DuplicateException | NotFoundException | IllegalArgumentException e) {
            throw new TaiKhoanFaultException(e.getMessage(), new TaiKhoanFaultInfo(e.getMessage()));
        } catch (DataAccessException e) {
            throw new TaiKhoanFaultException("Loi he thong: " + e.getMessage(), new TaiKhoanFaultInfo(e.getMessage()));
        }
    }

    private static TaiKhoanSoapDto toDto(TaiKhoanChungKhoan taiKhoan) {
        return new TaiKhoanSoapDto(
                taiKhoan.getSoTaiKhoan(),
                taiKhoan.getKhachHangId(),
                taiKhoan.getLoaiTaiKhoan().name(),
                taiKhoan.getTrangThai().name(),
                taiKhoan.getNgayMo().toString(),
                taiKhoan.getSoDuTien());
    }
}
