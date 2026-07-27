package com.demo.securities.oop;

import java.time.LocalDateTime;

/**
 * === RECORD (Java 16+) ===
 * Record la "immutable data carrier" — tu sinh constructor, getter, equals, hashCode, toString.
 * Khac class thuong: field tự final, khong setter, khong ke thua.
 * Dung khi chi can mang du lieu (DTO, event, value object).
 *
 * === ENCAPSULATION ===
 * Moi component cua record la private final — khong ai sua duoc sau khi tao.
 * Truy cap chi qua accessor method (soTaiKhoan(), soTien()...).
 */
public record GiaoDich(
        String maGiaoDich,
        String soTaiKhoan,
        LoaiGiaoDich loaiGiaoDich,
        double soTien,
        double phiGiaoDich,
        LocalDateTime thoiGian,
        String ghiChu
) {
    /**
     * Compact constructor — validate ngay luc tao, dam bao object luon hop le
     * (khong the tao GiaoDich voi soTien am). Day la encapsulation: an chi tiet
     * validate ben trong, nguoi dung chi thay constructor don gian.
     */
    public GiaoDich {
        if (soTien < 0) {
            throw new IllegalArgumentException("So tien giao dich khong duoc am: " + soTien);
        }
        if (phiGiaoDich < 0) {
            throw new IllegalArgumentException("Phi giao dich khong duoc am: " + phiGiaoDich);
        }
        if (maGiaoDich == null || maGiaoDich.isBlank()) {
            throw new IllegalArgumentException("Ma giao dich khong duoc trong");
        }
    }

    /**
     * Derived property — tinh toan tu field co san, khong luu rieng.
     */
    public double tongTien() {
        return soTien + phiGiaoDich;
    }

    /**
     * Kiem tra giao dich lon (tren 100 trieu).
     */
    public boolean isGiaoDichLon() {
        return soTien >= 100_000_000;
    }
}
