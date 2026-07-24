package com.demo.securities.springflux;

import com.demo.securities.model.GioiTinh;
import com.demo.securities.model.KhachHang;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.spring.dto.CreateKhachHangRequest;
import com.demo.securities.spring.dto.KhachHangDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.List;

/**
 * Bọc KhachHangService (JDBC blocking) qua Mono.fromCallable(...).subscribeOn(boundedElastic) —
 * "reactive wrapper trên DAO blocking cũ", không phải reactive thật từ đầu tới cuối (không
 * đổi sang R2DBC). Đây là cách phổ biến khi 1 team thêm WebFlux trên nền code cũ.
 */
@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangReactiveController {

    private final KhachHangService khachHangService;

    public KhachHangReactiveController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    @GetMapping
    public Mono<List<KhachHangDto>> list(@RequestParam(required = false) String ten) {
        return Mono.fromCallable(() -> {
            List<KhachHang> result = (ten == null || ten.isBlank())
                    ? khachHangService.danhSach()
                    : khachHangService.timTheoTen(ten);
            return result.stream().map(KhachHangReactiveController::toDto).toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<KhachHangDto> getById(@PathVariable String id) {
        return Mono.fromCallable(() -> toDto(khachHangService.timTheoId(id)))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping
    public Mono<ResponseEntity<KhachHangDto>> create(@RequestBody CreateKhachHangRequest req) {
        return Mono.fromCallable(() -> {
            KhachHang khachHang = khachHangService.themKhachHang(
                    req.hoTen(),
                    LocalDate.parse(req.ngaySinh()),
                    GioiTinh.valueOf(req.gioiTinh().toUpperCase()),
                    req.soCCCD(),
                    req.soDienThoai(),
                    req.email(),
                    req.diaChi());
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(khachHang));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PutMapping("/{id}")
    public Mono<KhachHangDto> update(@PathVariable String id, @RequestBody CreateKhachHangRequest req) {
        return Mono.fromCallable(() -> toDto(khachHangService.suaKhachHang(
                id,
                req.hoTen(),
                LocalDate.parse(req.ngaySinh()),
                GioiTinh.valueOf(req.gioiTinh().toUpperCase()),
                req.soDienThoai(),
                req.email(),
                req.diaChi()))).subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return Mono.fromCallable(() -> {
            khachHangService.xoaKhachHang(id);
            return ResponseEntity.noContent().<Void>build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private static KhachHangDto toDto(KhachHang khachHang) {
        return new KhachHangDto(
                khachHang.getId(),
                khachHang.getHoTen(),
                khachHang.getNgaySinh().toString(),
                khachHang.getGioiTinh().name(),
                khachHang.getSoCCCD(),
                khachHang.getSoDienThoai(),
                khachHang.getEmail(),
                khachHang.getDiaChi(),
                khachHang.getNgayTao().toString());
    }
}
