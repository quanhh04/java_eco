package com.demo.securities.spring;

import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.spring.dto.OpenTaiKhoanRequest;
import com.demo.securities.spring.dto.SoTienRequest;
import com.demo.securities.spring.dto.TaiKhoanDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tai-khoan")
public class TaiKhoanController {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @GetMapping
    public List<TaiKhoanDto> list(@RequestParam(required = false) String khachHangId) {
        List<TaiKhoanChungKhoan> result = (khachHangId == null || khachHangId.isBlank())
                ? taiKhoanService.danhSachTatCa()
                : taiKhoanService.danhSachTheoKhachHang(khachHangId);
        return result.stream().map(TaiKhoanController::toDto).toList();
    }

    @GetMapping("/{so}")
    public TaiKhoanDto getBySo(@PathVariable String so) {
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @PostMapping
    public ResponseEntity<TaiKhoanDto> open(@RequestBody OpenTaiKhoanRequest req) {
        double soDuBanDau = req.soDuBanDau() == null ? 0 : req.soDuBanDau();
        TaiKhoanChungKhoan taiKhoan = taiKhoanService.moTaiKhoan(
                req.khachHangId(),
                LoaiTaiKhoan.valueOf(req.loaiTaiKhoan().toUpperCase()),
                soDuBanDau);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(taiKhoan));
    }

    @PostMapping("/{so}/khoa")
    public TaiKhoanDto khoa(@PathVariable String so) {
        taiKhoanService.khoaTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @PostMapping("/{so}/mo-khoa")
    public TaiKhoanDto moKhoa(@PathVariable String so) {
        taiKhoanService.moKhoaTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @PostMapping("/{so}/dong")
    public TaiKhoanDto dong(@PathVariable String so) {
        taiKhoanService.dongTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @PostMapping("/{so}/nap")
    public TaiKhoanDto nap(@PathVariable String so, @RequestBody SoTienRequest req) {
        taiKhoanService.napTien(so, req.soTien());
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @PostMapping("/{so}/rut")
    public TaiKhoanDto rut(@PathVariable String so, @RequestBody SoTienRequest req) {
        taiKhoanService.rutTien(so, req.soTien());
        return toDto(taiKhoanService.timTheoSo(so));
    }

    private static TaiKhoanDto toDto(TaiKhoanChungKhoan taiKhoan) {
        return new TaiKhoanDto(
                taiKhoan.getSoTaiKhoan(),
                taiKhoan.getKhachHangId(),
                taiKhoan.getLoaiTaiKhoan().name(),
                taiKhoan.getTrangThai().name(),
                taiKhoan.getNgayMo().toString(),
                taiKhoan.getSoDuTien());
    }
}
