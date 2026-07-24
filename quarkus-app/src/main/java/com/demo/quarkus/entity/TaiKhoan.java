package com.demo.quarkus.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Idiom Panache: entity tu la "repository" luon (persist()/delete() ngay tren
 * instance, find()/list() la static method ke thua tu PanacheEntityBase) - khac
 * han DAO/EntityManager rieng cua SpringFrameworkMain (Hibernate thuan) da lam
 * o entry point khac. Dung PanacheEntityBase (khong phai PanacheEntity) vi khoa
 * chinh la String tu sinh o tang service, khong phai Long id tu dong tang.
 */
@Entity
@Table(name = "tai_khoan_chung_khoan")
public class TaiKhoan extends PanacheEntityBase {

    @Id
    @Column(name = "so_tai_khoan")
    public String soTaiKhoan;

    @Column(name = "khach_hang_id")
    public String khachHangId;

    @Column(name = "loai_tai_khoan")
    @Enumerated(EnumType.STRING)
    public LoaiTaiKhoan loaiTaiKhoan;

    @Column(name = "trang_thai")
    @Enumerated(EnumType.STRING)
    public TrangThaiTaiKhoan trangThai;

    @Column(name = "ngay_mo")
    public LocalDateTime ngayMo;

    @Column(name = "so_du_tien")
    public BigDecimal soDuTien;

    public static TaiKhoan timTheoSo(String so) {
        return findById(so);
    }

    public static List<TaiKhoan> danhSachTheoKhachHang(String khachHangId) {
        return list("khachHangId", khachHangId);
    }

    public enum LoaiTaiKhoan { CO_SO, KY_QUY }

    public enum TrangThaiTaiKhoan { HOAT_DONG, TAM_KHOA, DA_DONG }
}
