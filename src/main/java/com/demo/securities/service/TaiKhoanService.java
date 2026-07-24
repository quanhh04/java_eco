package com.demo.securities.service;

import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import com.demo.securities.model.KhachHang;
import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.model.TrangThaiTaiKhoan;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.util.IdGenerator;
import com.demo.securities.util.Validator;

import java.time.LocalDateTime;
import java.util.List;

public class TaiKhoanService {

    private static final String PREFIX = "TK";

    private final TaiKhoanRepository taiKhoanRepository;
    private final KhachHangService khachHangService;

    public TaiKhoanService(TaiKhoanRepository taiKhoanRepository, KhachHangService khachHangService) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.khachHangService = khachHangService;
    }

    public TaiKhoanChungKhoan moTaiKhoan(String khachHangId, LoaiTaiKhoan loaiTaiKhoan, double soDuBanDau) {
        KhachHang khachHang = khachHangService.timTheoId(khachHangId);
        if (!Validator.isAdult(khachHang.getNgaySinh())) {
            throw new ValidationException("Khách hàng phải đủ 18 tuổi để mở tài khoản chứng khoán");
        }
        if (soDuBanDau < 0) {
            throw new ValidationException("Số dư ban đầu không được âm");
        }
        String soTaiKhoan = IdGenerator.nextId(taiKhoanRepository.findAll(),
                TaiKhoanChungKhoan::getSoTaiKhoan, PREFIX, 6);
        TaiKhoanChungKhoan taiKhoan = new TaiKhoanChungKhoan(soTaiKhoan, khachHangId, loaiTaiKhoan,
                TrangThaiTaiKhoan.HOAT_DONG, LocalDateTime.now(), soDuBanDau);
        taiKhoanRepository.save(taiKhoan);
        return taiKhoan;
    }

    public void khoaTaiKhoan(String soTaiKhoan) {
        TaiKhoanChungKhoan taiKhoan = timTheoSo(soTaiKhoan);
        if (taiKhoan.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG) {
            throw new ValidationException("Chỉ có thể khóa tài khoản đang hoạt động");
        }
        taiKhoan.setTrangThai(TrangThaiTaiKhoan.TAM_KHOA);
        taiKhoanRepository.update(taiKhoan);
    }

    public void moKhoaTaiKhoan(String soTaiKhoan) {
        TaiKhoanChungKhoan taiKhoan = timTheoSo(soTaiKhoan);
        if (taiKhoan.getTrangThai() != TrangThaiTaiKhoan.TAM_KHOA) {
            throw new ValidationException("Chỉ có thể mở khóa tài khoản đang tạm khóa");
        }
        taiKhoan.setTrangThai(TrangThaiTaiKhoan.HOAT_DONG);
        taiKhoanRepository.update(taiKhoan);
    }

    public void dongTaiKhoan(String soTaiKhoan) {
        TaiKhoanChungKhoan taiKhoan = timTheoSo(soTaiKhoan);
        if (taiKhoan.getTrangThai() == TrangThaiTaiKhoan.DA_DONG) {
            throw new ValidationException("Tài khoản đã được đóng trước đó");
        }
        if (taiKhoan.getSoDuTien() != 0) {
            throw new ValidationException("Số dư phải bằng 0 trước khi đóng tài khoản");
        }
        taiKhoan.setTrangThai(TrangThaiTaiKhoan.DA_DONG);
        taiKhoanRepository.update(taiKhoan);
    }

    public void napTien(String soTaiKhoan, double soTien) {
        TaiKhoanChungKhoan taiKhoan = timTheoSo(soTaiKhoan);
        kiemTraHoatDong(taiKhoan);
        if (soTien <= 0) {
            throw new ValidationException("Số tiền nạp phải lớn hơn 0");
        }
        taiKhoan.setSoDuTien(taiKhoan.getSoDuTien() + soTien);
        taiKhoanRepository.update(taiKhoan);
    }

    public void rutTien(String soTaiKhoan, double soTien) {
        TaiKhoanChungKhoan taiKhoan = timTheoSo(soTaiKhoan);
        kiemTraHoatDong(taiKhoan);
        if (soTien <= 0) {
            throw new ValidationException("Số tiền rút phải lớn hơn 0");
        }
        if (soTien > taiKhoan.getSoDuTien()) {
            throw new ValidationException("Số dư không đủ để rút");
        }
        taiKhoan.setSoDuTien(taiKhoan.getSoDuTien() - soTien);
        taiKhoanRepository.update(taiKhoan);
    }

    public TaiKhoanChungKhoan timTheoSo(String soTaiKhoan) {
        return taiKhoanRepository.findBySoTaiKhoan(soTaiKhoan)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản: " + soTaiKhoan));
    }

    public List<TaiKhoanChungKhoan> danhSachTheoKhachHang(String khachHangId) {
        khachHangService.timTheoId(khachHangId);
        return taiKhoanRepository.findByKhachHangId(khachHangId);
    }

    public List<TaiKhoanChungKhoan> danhSachTatCa() {
        return taiKhoanRepository.findAll();
    }

    private void kiemTraHoatDong(TaiKhoanChungKhoan taiKhoan) {
        if (taiKhoan.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG) {
            throw new ValidationException("Tài khoản không ở trạng thái hoạt động");
        }
    }
}
