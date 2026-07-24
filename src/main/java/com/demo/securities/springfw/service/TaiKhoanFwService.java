package com.demo.securities.springfw.service;

import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import com.demo.securities.model.KhachHang;
import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TrangThaiTaiKhoan;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.springfw.dao.TaiKhoanDao;
import com.demo.securities.springfw.entity.TaiKhoanEntity;
import com.demo.securities.util.IdGenerator;
import com.demo.securities.util.Validator;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class TaiKhoanFwService {

    private static final String PREFIX = "TK";

    private final TaiKhoanDao taiKhoanDao;
    private final KhachHangService khachHangService;

    public TaiKhoanFwService(TaiKhoanDao taiKhoanDao, KhachHangService khachHangService) {
        this.taiKhoanDao = taiKhoanDao;
        this.khachHangService = khachHangService;
    }

    @Transactional
    public TaiKhoanEntity moTaiKhoan(String khachHangId, LoaiTaiKhoan loaiTaiKhoan, double soDuBanDau) {
        KhachHang khachHang = khachHangService.timTheoId(khachHangId);
        if (!Validator.isAdult(khachHang.getNgaySinh())) {
            throw new ValidationException("Khach hang phai du 18 tuoi de mo tai khoan chung khoan");
        }
        if (soDuBanDau < 0) {
            throw new ValidationException("So du ban dau khong duoc am");
        }
        String soTaiKhoan = IdGenerator.nextId(taiKhoanDao.findAllSoTaiKhoan(), id -> id, PREFIX, 6);
        TaiKhoanEntity entity = new TaiKhoanEntity(soTaiKhoan, khachHangId, loaiTaiKhoan,
                TrangThaiTaiKhoan.HOAT_DONG, LocalDateTime.now(), soDuBanDau);
        taiKhoanDao.save(entity);
        return entity;
    }

    @Transactional(readOnly = true)
    public TaiKhoanEntity truyVanTaiKhoan(String soTaiKhoan) {
        return timTheoSo(soTaiKhoan);
    }

    @Transactional(readOnly = true)
    public List<TaiKhoanEntity> danhSachTatCa() {
        return taiKhoanDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<TaiKhoanEntity> danhSachTheoKhachHang(String khachHangId) {
        return taiKhoanDao.findByKhachHangId(khachHangId);
    }

    @Transactional
    public void khoaTaiKhoan(String soTaiKhoan) {
        TaiKhoanEntity taiKhoan = timTheoSo(soTaiKhoan);
        if (taiKhoan.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG) {
            throw new ValidationException("Chi co the khoa tai khoan dang hoat dong");
        }
        taiKhoan.setTrangThai(TrangThaiTaiKhoan.TAM_KHOA);
    }

    @Transactional
    public void moKhoaTaiKhoan(String soTaiKhoan) {
        TaiKhoanEntity taiKhoan = timTheoSo(soTaiKhoan);
        if (taiKhoan.getTrangThai() != TrangThaiTaiKhoan.TAM_KHOA) {
            throw new ValidationException("Chi co the mo khoa tai khoan dang tam khoa");
        }
        taiKhoan.setTrangThai(TrangThaiTaiKhoan.HOAT_DONG);
    }

    @Transactional
    public void dongTaiKhoan(String soTaiKhoan) {
        TaiKhoanEntity taiKhoan = timTheoSo(soTaiKhoan);
        if (taiKhoan.getTrangThai() == TrangThaiTaiKhoan.DA_DONG) {
            throw new ValidationException("Tai khoan da duoc dong truoc do");
        }
        if (taiKhoan.getSoDuTien() != 0) {
            throw new ValidationException("So du phai bang 0 truoc khi dong tai khoan");
        }
        taiKhoan.setTrangThai(TrangThaiTaiKhoan.DA_DONG);
    }

    @Transactional
    public void napTien(String soTaiKhoan, double soTien) {
        TaiKhoanEntity taiKhoan = timTheoSo(soTaiKhoan);
        kiemTraHoatDong(taiKhoan);
        if (soTien <= 0) {
            throw new ValidationException("So tien nap phai lon hon 0");
        }
        taiKhoan.setSoDuTien(taiKhoan.getSoDuTien() + soTien);
    }

    @Transactional
    public void rutTien(String soTaiKhoan, double soTien) {
        TaiKhoanEntity taiKhoan = timTheoSo(soTaiKhoan);
        kiemTraHoatDong(taiKhoan);
        if (soTien <= 0) {
            throw new ValidationException("So tien rut phai lon hon 0");
        }
        if (soTien > taiKhoan.getSoDuTien()) {
            throw new ValidationException("So du khong du de rut");
        }
        taiKhoan.setSoDuTien(taiKhoan.getSoDuTien() - soTien);
    }

    /**
     * Minh hoa Transaction Management thật sự: trừ tiền tài khoản nguồn trước
     * (Hibernate dirty-checking sẽ tự flush lúc commit, không cần gọi persist/merge
     * tường minh vì entity đã được quản lý trong cùng 1 EntityManager/transaction),
     * sau đó tìm tài khoản đích — nếu đích không tồn tại/không hoạt động, exception
     * ném ra khiến TOÀN BỘ transaction rollback, bao gồm cả phần trừ tiền nguồn vừa
     * làm ở trên (chưa từng thật sự commit xuống DB).
     */
    @Transactional
    public TaiKhoanEntity chuyenTien(String soNguon, String soDich, double soTien) {
        if (soTien <= 0) {
            throw new ValidationException("So tien chuyen phai lon hon 0");
        }
        TaiKhoanEntity nguon = timTheoSo(soNguon);
        kiemTraHoatDong(nguon);
        if (soTien > nguon.getSoDuTien()) {
            throw new ValidationException("So du tai khoan nguon khong du de chuyen");
        }
        nguon.setSoDuTien(nguon.getSoDuTien() - soTien);

        TaiKhoanEntity dich = timTheoSo(soDich);
        kiemTraHoatDong(dich);
        dich.setSoDuTien(dich.getSoDuTien() + soTien);

        return nguon;
    }

    private TaiKhoanEntity timTheoSo(String soTaiKhoan) {
        TaiKhoanEntity taiKhoan = taiKhoanDao.find(soTaiKhoan);
        if (taiKhoan == null) {
            throw new NotFoundException("Khong tim thay tai khoan: " + soTaiKhoan);
        }
        return taiKhoan;
    }

    private void kiemTraHoatDong(TaiKhoanEntity taiKhoan) {
        if (taiKhoan.getTrangThai() != TrangThaiTaiKhoan.HOAT_DONG) {
            throw new ValidationException("Tai khoan khong o trang thai hoat dong: " + taiKhoan.getSoTaiKhoan());
        }
    }
}
