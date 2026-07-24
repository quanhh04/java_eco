package com.demo.securities.service;

import com.demo.securities.exception.DuplicateException;
import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import com.demo.securities.model.GioiTinh;
import com.demo.securities.model.KhachHang;
import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.util.IdGenerator;
import com.demo.securities.util.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class KhachHangService {

    private static final String PREFIX = "KH";

    private final KhachHangRepository khachHangRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    public KhachHangService(KhachHangRepository khachHangRepository, TaiKhoanRepository taiKhoanRepository) {
        this.khachHangRepository = khachHangRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    public KhachHang themKhachHang(String hoTen, LocalDate ngaySinh, GioiTinh gioiTinh,
                                    String soCCCD, String soDienThoai, String email, String diaChi) {
        validate(hoTen, ngaySinh, soCCCD, soDienThoai, email);
        if (khachHangRepository.findByCCCD(soCCCD).isPresent()) {
            throw new DuplicateException("Số CCCD đã tồn tại: " + soCCCD);
        }
        String id = IdGenerator.nextId(khachHangRepository.findAll(), KhachHang::getId, PREFIX, 4);
        KhachHang khachHang = new KhachHang(id, hoTen, ngaySinh, gioiTinh, soCCCD, soDienThoai,
                email, diaChi, LocalDateTime.now());
        khachHangRepository.save(khachHang);
        return khachHang;
    }

    public KhachHang suaKhachHang(String id, String hoTen, LocalDate ngaySinh, GioiTinh gioiTinh,
                                   String soDienThoai, String email, String diaChi) {
        KhachHang khachHang = timTheoId(id);
        validate(hoTen, ngaySinh, khachHang.getSoCCCD(), soDienThoai, email);
        khachHang.setHoTen(hoTen);
        khachHang.setNgaySinh(ngaySinh);
        khachHang.setGioiTinh(gioiTinh);
        khachHang.setSoDienThoai(soDienThoai);
        khachHang.setEmail(email);
        khachHang.setDiaChi(diaChi);
        khachHangRepository.update(khachHang);
        return khachHang;
    }

    public void xoaKhachHang(String id) {
        timTheoId(id);
        if (!taiKhoanRepository.findByKhachHangId(id).isEmpty()) {
            throw new ValidationException("Không thể xóa khách hàng đã có tài khoản chứng khoán");
        }
        khachHangRepository.deleteById(id);
    }

    public KhachHang timTheoId(String id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng có mã: " + id));
    }

    public List<KhachHang> timTheoTen(String keyword) {
        return khachHangRepository.searchByTen(keyword);
    }

    public List<KhachHang> danhSach() {
        return khachHangRepository.findAll();
    }

    private void validate(String hoTen, LocalDate ngaySinh, String soCCCD, String soDienThoai, String email) {
        if (Validator.isBlank(hoTen)) {
            throw new ValidationException("Họ tên không được để trống");
        }
        if (ngaySinh == null || ngaySinh.isAfter(LocalDate.now())) {
            throw new ValidationException("Ngày sinh không hợp lệ");
        }
        if (!Validator.isValidCCCD(soCCCD)) {
            throw new ValidationException("Số CCCD phải gồm đúng 12 chữ số");
        }
        if (!Validator.isValidPhone(soDienThoai)) {
            throw new ValidationException("Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)");
        }
        if (!Validator.isValidEmail(email)) {
            throw new ValidationException("Email không hợp lệ");
        }
    }
}
