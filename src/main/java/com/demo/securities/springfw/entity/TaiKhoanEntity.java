package com.demo.securities.springfw.entity;

import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TrangThaiTaiKhoan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

/**
 * Map thẳng vào bảng tai_khoan_chung_khoan đã có sẵn (không để Hibernate tự tạo/sửa —
 * xem hibernate.hbm2ddl.auto=none trong JpaConfig). Dùng lại enum có sẵn của domain,
 * không tạo enum riêng cho lớp JPA này.
 */
@Entity
@Table(name = "tai_khoan_chung_khoan")
public class TaiKhoanEntity {

    @Id
    @Column(name = "so_tai_khoan")
    private String soTaiKhoan;

    @Column(name = "khach_hang_id")
    private String khachHangId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_tai_khoan")
    private LoaiTaiKhoan loaiTaiKhoan;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiTaiKhoan trangThai;

    @Column(name = "ngay_mo")
    private LocalDateTime ngayMo;

    @Column(name = "so_du_tien")
    private double soDuTien;

    // Optimistic lock: Hibernate tu doc gia tri nay luc SELECT, tu them "AND version = ?"
    // vao cau UPDATE va tu tang len 1 luc flush - khong can code tay xu ly. Neu UPDATE
    // dung 0 dong (nghia la ban ghi da bi ai do sua/commit truoc, version DB da khac),
    // Hibernate nem OptimisticLockException thay vi am tham ghi de (lost update). Cot
    // "version" chi duoc Hibernate/JPA tren nhanh nay quan ly - cac entry point JDBC
    // thuan (TaiKhoanRepositoryImpl) khong dung, INSERT/UPDATE cua chung khong bump
    // version nen khong tham gia bao ve nay.
    @Version
    @Column(name = "version")
    private long version;

    protected TaiKhoanEntity() {
        // constructor no-arg protected - JPA yeu cau, khong dung truc tiep tu code ung dung
    }

    public TaiKhoanEntity(String soTaiKhoan, String khachHangId, LoaiTaiKhoan loaiTaiKhoan,
                          TrangThaiTaiKhoan trangThai, LocalDateTime ngayMo, double soDuTien) {
        this.soTaiKhoan = soTaiKhoan;
        this.khachHangId = khachHangId;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.trangThai = trangThai;
        this.ngayMo = ngayMo;
        this.soDuTien = soDuTien;
    }

    public String getSoTaiKhoan() {
        return soTaiKhoan;
    }

    public String getKhachHangId() {
        return khachHangId;
    }

    public LoaiTaiKhoan getLoaiTaiKhoan() {
        return loaiTaiKhoan;
    }

    public TrangThaiTaiKhoan getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiTaiKhoan trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getNgayMo() {
        return ngayMo;
    }

    public double getSoDuTien() {
        return soDuTien;
    }

    public void setSoDuTien(double soDuTien) {
        this.soDuTien = soDuTien;
    }

    public long getVersion() {
        return version;
    }
}
