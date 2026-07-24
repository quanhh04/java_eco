package com.demo.securities.springfw.web;

import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.spring.dto.OpenTaiKhoanRequest;
import com.demo.securities.spring.dto.SoTienRequest;
import com.demo.securities.spring.dto.TaiKhoanDto;
import com.demo.securities.springfw.entity.TaiKhoanEntity;
import com.demo.securities.springfw.service.OptimisticLockDemoService;
import com.demo.securities.springfw.service.TaiKhoanFwService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tai-khoan")
public class TaiKhoanFwController {

    private final TaiKhoanFwService taiKhoanFwService;
    private final OptimisticLockDemoService optimisticLockDemoService;

    public TaiKhoanFwController(TaiKhoanFwService taiKhoanFwService,
                                 OptimisticLockDemoService optimisticLockDemoService) {
        this.taiKhoanFwService = taiKhoanFwService;
        this.optimisticLockDemoService = optimisticLockDemoService;
    }

    @GetMapping
    public List<TaiKhoanDto> list(@RequestParam(required = false) String khachHangId) {
        List<TaiKhoanEntity> result = (khachHangId == null || khachHangId.isBlank())
                ? taiKhoanFwService.danhSachTatCa()
                : taiKhoanFwService.danhSachTheoKhachHang(khachHangId);
        return result.stream().map(TaiKhoanFwController::toDto).toList();
    }

    @GetMapping("/{so}")
    public TaiKhoanDto getBySo(@PathVariable String so) {
        return toDto(taiKhoanFwService.truyVanTaiKhoan(so));
    }

    @PostMapping
    public ResponseEntity<TaiKhoanDto> open(@RequestBody OpenTaiKhoanRequest req) {
        double soDuBanDau = req.soDuBanDau() == null ? 0 : req.soDuBanDau();
        TaiKhoanEntity taiKhoan = taiKhoanFwService.moTaiKhoan(
                req.khachHangId(),
                LoaiTaiKhoan.valueOf(req.loaiTaiKhoan().toUpperCase()),
                soDuBanDau);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(taiKhoan));
    }

    @PostMapping("/{so}/khoa")
    public TaiKhoanDto khoa(@PathVariable String so) {
        taiKhoanFwService.khoaTaiKhoan(so);
        return toDto(taiKhoanFwService.truyVanTaiKhoan(so));
    }

    @PostMapping("/{so}/mo-khoa")
    public TaiKhoanDto moKhoa(@PathVariable String so) {
        taiKhoanFwService.moKhoaTaiKhoan(so);
        return toDto(taiKhoanFwService.truyVanTaiKhoan(so));
    }

    @PostMapping("/{so}/dong")
    public TaiKhoanDto dong(@PathVariable String so) {
        taiKhoanFwService.dongTaiKhoan(so);
        return toDto(taiKhoanFwService.truyVanTaiKhoan(so));
    }

    @PostMapping("/{so}/nap")
    public TaiKhoanDto nap(@PathVariable String so, @RequestBody SoTienRequest req) {
        taiKhoanFwService.napTien(so, req.soTien());
        return toDto(taiKhoanFwService.truyVanTaiKhoan(so));
    }

    @PostMapping("/{so}/rut")
    public TaiKhoanDto rut(@PathVariable String so, @RequestBody SoTienRequest req) {
        taiKhoanFwService.rutTien(so, req.soTien());
        return toDto(taiKhoanFwService.truyVanTaiKhoan(so));
    }

    @PostMapping("/{so}/chuyen-den/{soDich}")
    public TaiKhoanDto chuyenTien(@PathVariable String so, @PathVariable String soDich,
                                   @RequestBody SoTienRequest req) {
        taiKhoanFwService.chuyenTien(so, soDich, req.soTien());
        return toDto(taiKhoanFwService.truyVanTaiKhoan(so));
    }

    /**
     * Demo Optimistic Lock: luon tai hien dung 1 xung dot (khong phai chay may lan
     * moi trung 1 lan nhu race dua theo thread that) - xem OptimisticLockDemoService
     * de biet co che. Ky vong tra ve 409 CONFLICT (GlobalExceptionHandler bat
     * ObjectOptimisticLockingFailureException) thay vi 200 OK ghi de am tham.
     */
    @PostMapping("/{so}/demo-optimistic-lock")
    public ResponseEntity<Map<String, String>> demoOptimisticLock(@PathVariable String so) {
        optimisticLockDemoService.demoXungDot(so, 100, 100);
        return ResponseEntity.ok(Map.of("ketQua", "Khong xung dot (khong nen xay ra voi demo nay)"));
    }

    private static TaiKhoanDto toDto(TaiKhoanEntity taiKhoan) {
        return new TaiKhoanDto(
                taiKhoan.getSoTaiKhoan(),
                taiKhoan.getKhachHangId(),
                taiKhoan.getLoaiTaiKhoan().name(),
                taiKhoan.getTrangThai().name(),
                taiKhoan.getNgayMo().toString(),
                taiKhoan.getSoDuTien());
    }
}
