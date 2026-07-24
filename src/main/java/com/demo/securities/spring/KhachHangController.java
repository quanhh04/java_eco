package com.demo.securities.spring;

import com.demo.securities.model.GioiTinh;
import com.demo.securities.model.KhachHang;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.spring.dto.CreateKhachHangRequest;
import com.demo.securities.spring.dto.KhachHangDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangController {

    private final KhachHangService khachHangService;

    public KhachHangController(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    @GetMapping
    public List<KhachHangDto> list(@RequestParam(required = false) String ten) {
        List<KhachHang> result = (ten == null || ten.isBlank())
                ? khachHangService.danhSach()
                : khachHangService.timTheoTen(ten);
        return result.stream().map(KhachHangController::toDto).toList();
    }

    @GetMapping("/{id}")
    public KhachHangDto getById(@PathVariable String id) {
        return toDto(khachHangService.timTheoId(id));
    }

    @PostMapping
    public ResponseEntity<KhachHangDto> create(@RequestBody CreateKhachHangRequest req) {
        KhachHang khachHang = khachHangService.themKhachHang(
                req.hoTen(),
                LocalDate.parse(req.ngaySinh()),
                GioiTinh.valueOf(req.gioiTinh().toUpperCase()),
                req.soCCCD(),
                req.soDienThoai(),
                req.email(),
                req.diaChi());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(khachHang));
    }

    @PutMapping("/{id}")
    public KhachHangDto update(@PathVariable String id, @RequestBody CreateKhachHangRequest req) {
        KhachHang khachHang = khachHangService.suaKhachHang(
                id,
                req.hoTen(),
                LocalDate.parse(req.ngaySinh()),
                GioiTinh.valueOf(req.gioiTinh().toUpperCase()),
                req.soDienThoai(),
                req.email(),
                req.diaChi());
        return toDto(khachHang);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        khachHangService.xoaKhachHang(id);
        return ResponseEntity.noContent().build();
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
