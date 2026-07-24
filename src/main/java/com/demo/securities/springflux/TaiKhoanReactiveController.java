package com.demo.securities.springflux;

import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.spring.dto.OpenTaiKhoanRequest;
import com.demo.securities.spring.dto.SoTienRequest;
import com.demo.securities.spring.dto.TaiKhoanDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/tai-khoan")
public class TaiKhoanReactiveController {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanReactiveController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @GetMapping
    public Mono<List<TaiKhoanDto>> list(@RequestParam(required = false) String khachHangId) {
        return Mono.fromCallable(() -> {
            List<TaiKhoanChungKhoan> result = (khachHangId == null || khachHangId.isBlank())
                    ? taiKhoanService.danhSachTatCa()
                    : taiKhoanService.danhSachTheoKhachHang(khachHangId);
            return result.stream().map(TaiKhoanReactiveController::toDto).toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{so}")
    public Mono<TaiKhoanDto> getBySo(@PathVariable String so) {
        return Mono.fromCallable(() -> toDto(taiKhoanService.timTheoSo(so)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping
    public Mono<ResponseEntity<TaiKhoanDto>> open(@RequestBody OpenTaiKhoanRequest req) {
        return Mono.fromCallable(() -> {
            double soDuBanDau = req.soDuBanDau() == null ? 0 : req.soDuBanDau();
            TaiKhoanChungKhoan taiKhoan = taiKhoanService.moTaiKhoan(
                    req.khachHangId(),
                    LoaiTaiKhoan.valueOf(req.loaiTaiKhoan().toUpperCase()),
                    soDuBanDau);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(taiKhoan));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/{so}/khoa")
    public Mono<TaiKhoanDto> khoa(@PathVariable String so) {
        return Mono.fromCallable(() -> {
            taiKhoanService.khoaTaiKhoan(so);
            return toDto(taiKhoanService.timTheoSo(so));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/{so}/mo-khoa")
    public Mono<TaiKhoanDto> moKhoa(@PathVariable String so) {
        return Mono.fromCallable(() -> {
            taiKhoanService.moKhoaTaiKhoan(so);
            return toDto(taiKhoanService.timTheoSo(so));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/{so}/dong")
    public Mono<TaiKhoanDto> dong(@PathVariable String so) {
        return Mono.fromCallable(() -> {
            taiKhoanService.dongTaiKhoan(so);
            return toDto(taiKhoanService.timTheoSo(so));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/{so}/nap")
    public Mono<TaiKhoanDto> nap(@PathVariable String so, @RequestBody SoTienRequest req) {
        return Mono.fromCallable(() -> {
            taiKhoanService.napTien(so, req.soTien());
            return toDto(taiKhoanService.timTheoSo(so));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/{so}/rut")
    public Mono<TaiKhoanDto> rut(@PathVariable String so, @RequestBody SoTienRequest req) {
        return Mono.fromCallable(() -> {
            taiKhoanService.rutTien(so, req.soTien());
            return toDto(taiKhoanService.timTheoSo(so));
        }).subscribeOn(Schedulers.boundedElastic());
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
