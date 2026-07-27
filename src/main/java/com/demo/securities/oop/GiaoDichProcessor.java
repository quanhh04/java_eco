package com.demo.securities.oop;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * === COMPOSITION, INTERFACE DUNG NHU TYPE, POLYMORPHISM TAI RUNTIME ===
 * Class nay xu ly giao dich: tinh phi, ghi log, phat su kien.
 *
 * OOP concepts minh hoa:
 * - Composition: GiaoDichProcessor "HAS-A" ChinhSachPhi (khong ke thua, chi so huu)
 * - Coding to interface: field kieu ChinhSachPhi (interface), khong phai class cu the
 *   → doi chinh sach bat ky luc nao (runtime polymorphism)
 * - Encapsulation: lichSu la private, chi doc qua getter (tra ban copy)
 * - Static field/method vs Instance field/method
 */
public class GiaoDichProcessor {

    // === STATIC field: thuoc ve CLASS, khong thuoc ve instance nao ===
    // Moi instance dung chung 1 bo dem (de minh hoa — thuc te dung AtomicLong)
    private static int boDemGiaoDich = 0;

    // === INSTANCE field: moi instance co rieng ===
    private ChinhSachPhi chinhSachPhi; // khong final → co the doi (setChinhSachPhi)
    private final String soTaiKhoan;
    private final List<GiaoDich> lichSu = new ArrayList<>(); // encapsulation: private, mutable ben trong

    /**
     * === CONSTRUCTOR ===
     * Nhan ChinhSachPhi (interface type) — khong quan tam implementation cu the.
     * Truyen ChinhSachPhiCoSo, ChinhSachPhiVip, hay bat ky class implement ChinhSachPhi
     * → deu chay dung. Day la POLYMORPHISM qua interface.
     */
    public GiaoDichProcessor(String soTaiKhoan, ChinhSachPhi chinhSachPhi) {
        this.soTaiKhoan = soTaiKhoan;
        this.chinhSachPhi = chinhSachPhi;
    }

    /**
     * === STATIC METHOD ===
     * Goi bang GiaoDichProcessor.sinhMaGiaoDich() — khong can instance.
     * Thao tac tren static field (boDemGiaoDich).
     */
    private static synchronized String sinhMaGiaoDich() {
        boDemGiaoDich++;
        return "GD" + String.format("%06d", boDemGiaoDich);
    }

    /**
     * === POLYMORPHISM TAI RUNTIME ===
     * chinhSachPhi.tinhPhiThucTe() → JVM quyet dinh goi method cua class NAO
     * tai THOI DIEM CHAY (runtime), tuy vao object thuc su duoc truyen vao
     * (ChinhSachPhiCoSo? ChinhSachPhiVip?). Day goi la "dynamic dispatch".
     */
    public SuKienGiaoDich thucHienGiaoDich(LoaiGiaoDich loaiGiaoDich, double soTien) {
        try {
            // Validate
            if (soTien <= 0) {
                return new SuKienGiaoDich.GiaoDichThatBai(soTaiKhoan, loaiGiaoDich,
                        "So tien phai lon hon 0");
            }

            // Tinh phi — POLYMORPHISM: tuy thuoc chinhSachPhi la loai nao
            double phi = chinhSachPhi.tinhPhiThucTe(loaiGiaoDich, soTien);

            // Tao record GiaoDich (immutable)
            GiaoDich giaoDich = new GiaoDich(
                    sinhMaGiaoDich(),
                    soTaiKhoan,
                    loaiGiaoDich,
                    soTien,
                    phi,
                    LocalDateTime.now(),
                    "Giao dich " + loaiGiaoDich.getMoTa()
            );

            // Ghi vao lich su (encapsulated — chi class nay moi add duoc)
            lichSu.add(giaoDich);

            return new SuKienGiaoDich.GiaoDichThanhCong(soTaiKhoan, giaoDich);

        } catch (Exception e) {
            return new SuKienGiaoDich.GiaoDichThatBai(soTaiKhoan, loaiGiaoDich,
                    "Loi he thong: " + e.getMessage());
        }
    }

    // === ENCAPSULATION: tra ban KHONG THE SUA (unmodifiable) cua list noi bo ===
    public List<GiaoDich> getLichSu() {
        return Collections.unmodifiableList(lichSu);
    }

    /**
     * === SETTER — doi chinh sach phi luc runtime ===
     * Vi field kieu INTERFACE (khong phai class cu the), co the doi bat ky luc nao.
     * VD: khach hang duoc nang cap VIP → doi ChinhSachPhiCoSo → ChinhSachPhiVip
     * ma KHONG can sua bat ky code nao khac trong class nay.
     * Day la Open/Closed Principle: "open for extension, closed for modification".
     */
    public void setChinhSachPhi(ChinhSachPhi chinhSachPhi) {
        this.chinhSachPhi = chinhSachPhi;
    }

    public ChinhSachPhi getChinhSachPhi() {
        return chinhSachPhi;
    }

    public String getSoTaiKhoan() {
        return soTaiKhoan;
    }

    /**
     * === toString() — OVERRIDING Object method ===
     */
    @Override
    public String toString() {
        return String.format("GiaoDichProcessor[%s, chinh sach: %s, so giao dich: %d]",
                soTaiKhoan, chinhSachPhi.tenChinhSach(), lichSu.size());
    }
}
