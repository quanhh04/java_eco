package com.demo.securities.oop;

import java.time.LocalDateTime;

/**
 * === SEALED CLASS (Java 17+) ===
 * Sealed gioi han "ai duoc phep ke thua" — chi cac class listed trong permits.
 * Compiler biet CHINH XAC co bao nhieu subtype → pattern matching (switch) exhaustive.
 *
 * Khac:
 * - class thuong: bat ky ai cung ke thua duoc (khong kiem soat)
 * - final class: KHONG AI ke thua duoc
 * - sealed class: CHI nhung class duoc phep moi ke thua (kiem soat chinh xac)
 *
 * Moi subclass phai la: final, sealed, hoac non-sealed.
 *
 * Nghiep vu: Su kien phat sinh tu giao dich — dung cho thong bao/audit log.
 */
public sealed abstract class SuKienGiaoDich
        permits SuKienGiaoDich.GiaoDichThanhCong,
                SuKienGiaoDich.GiaoDichThatBai,
                SuKienGiaoDich.GiaoDichChoXuLy {

    private final String soTaiKhoan;
    private final LocalDateTime thoiGian;

    protected SuKienGiaoDich(String soTaiKhoan) {
        this.soTaiKhoan = soTaiKhoan;
        this.thoiGian = LocalDateTime.now();
    }

    public String getSoTaiKhoan() { return soTaiKhoan; }
    public LocalDateTime getThoiGian() { return thoiGian; }

    /** Method chung cho moi su kien — subclass override de cung cap chi tiet rieng */
    public abstract String moTa();

    // =====================================================================
    // === INNER CLASS (Static nested class) ===
    // Class dinh nghia BEN TRONG class khac. Static nested class khong can
    // instance cua outer class de ton tai — dung khi inner class lien quan
    // chat ve logic nhung khong can truy cap field instance cua outer.
    // =====================================================================

    /**
     * === FINAL class ke thua sealed ===
     * Final = khong ai ke thua tiep tu day (la dinh cua cay).
     */
    public static final class GiaoDichThanhCong extends SuKienGiaoDich {
        private final GiaoDich giaoDich;

        public GiaoDichThanhCong(String soTaiKhoan, GiaoDich giaoDich) {
            super(soTaiKhoan);
            this.giaoDich = giaoDich;
        }

        public GiaoDich getGiaoDich() { return giaoDich; }

        @Override
        public String moTa() {
            return String.format("[THANH CONG] %s - %s: %,.0f (phi: %,.0f)",
                    getSoTaiKhoan(), giaoDich.loaiGiaoDich().getMoTa(),
                    giaoDich.soTien(), giaoDich.phiGiaoDich());
        }
    }

    public static final class GiaoDichThatBai extends SuKienGiaoDich {
        private final String lyDo;
        private final LoaiGiaoDich loaiGiaoDich;

        public GiaoDichThatBai(String soTaiKhoan, LoaiGiaoDich loaiGiaoDich, String lyDo) {
            super(soTaiKhoan);
            this.lyDo = lyDo;
            this.loaiGiaoDich = loaiGiaoDich;
        }

        public String getLyDo() { return lyDo; }
        public LoaiGiaoDich getLoaiGiaoDich() { return loaiGiaoDich; }

        @Override
        public String moTa() {
            return String.format("[THAT BAI] %s - %s: %s",
                    getSoTaiKhoan(), loaiGiaoDich.getMoTa(), lyDo);
        }
    }

    public static final class GiaoDichChoXuLy extends SuKienGiaoDich {
        private final double soTien;
        private final LoaiGiaoDich loaiGiaoDich;

        public GiaoDichChoXuLy(String soTaiKhoan, LoaiGiaoDich loaiGiaoDich, double soTien) {
            super(soTaiKhoan);
            this.soTien = soTien;
            this.loaiGiaoDich = loaiGiaoDich;
        }

        public double getSoTien() { return soTien; }
        public LoaiGiaoDich getLoaiGiaoDich() { return loaiGiaoDich; }

        @Override
        public String moTa() {
            return String.format("[CHO XU LY] %s - %s: %,.0f",
                    getSoTaiKhoan(), loaiGiaoDich.getMoTa(), soTien);
        }
    }

    /**
     * === PATTERN MATCHING voi SEALED CLASS (Java 21+) ===
     * Compiler biet sealed class chi co 3 subtype → switch EXHAUSTIVE
     * (khong can default, neu thieu 1 case → compiler bao loi).
     */
    public static String xuLySuKien(SuKienGiaoDich suKien) {
        return switch (suKien) {
            case GiaoDichThanhCong tc -> "Gui thong bao thanh cong: " + tc.getGiaoDich().maGiaoDich();
            case GiaoDichThatBai tb -> "Canh bao that bai: " + tb.getLyDo();
            case GiaoDichChoXuLy cx -> "Dat vao hang doi xu ly: " + cx.getSoTien();
        };
    }
}
