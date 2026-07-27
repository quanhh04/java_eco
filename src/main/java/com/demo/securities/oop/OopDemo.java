package com.demo.securities.oop;

import java.time.LocalDateTime;
import java.util.List;

/**
 * === DEMO TONG HOP — CHAY TRUC TIEP DE THAY KET QUA ===
 *
 * Chay: mvn compile exec:java -Dexec.mainClass=com.demo.securities.oop.OopDemo
 * (Khong can DB, khong can db.properties — package nay doc lap hoan toan)
 *
 * Tong ket cac khai niem OOP minh hoa trong package nay:
 *
 * 1. ENCAPSULATION (Dong goi):
 *    - private field + public getter/setter (model, AbstractChinhSachPhi)
 *    - validate trong constructor/setter (GiaoDich compact constructor)
 *    - Collections.unmodifiableList() tra ra ngoai (GiaoDichProcessor.getLichSu)
 *    - Record: field tu dong private final (GiaoDich)
 *
 * 2. INHERITANCE (Ke thua):
 *    - AbstractChinhSachPhi → ChinhSachPhiCoSo / ChinhSachPhiKyQuy / ChinhSachPhiVip
 *    - SuKienGiaoDich (sealed) → GiaoDichThanhCong / GiaoDichThatBai / GiaoDichChoXuLy
 *    - super() goi constructor cha
 *    - Override method (tinhPhiCoSo, moTa, phiToiThieu, phiToiDa)
 *
 * 3. POLYMORPHISM (Da hinh):
 *    - Interface ChinhSachPhi: cung goi tinhPhi() nhung moi impl tinh khac
 *    - Enum LoaiGiaoDich: cung goi tinhPhiCoSo() nhung MUA/BAN/CHUYEN khac nhau
 *    - Sealed + pattern matching switch (SuKienGiaoDich.xuLySuKien)
 *    - GiaoDichProcessor: doi chinhSachPhi runtime → output thay doi
 *    - GiaoDichListener (functional interface): moi lambda la 1 "class" khac nhau
 *
 * 4. ABSTRACTION (Truu tuong):
 *    - Interface ChinhSachPhi: chi dinh nghia contract, khong dinh nghia cach lam
 *    - Abstract class AbstractChinhSachPhi: template method pattern
 *    - Abstract method tinhPhiCoSo(): bat buoc subclass implement
 *    - Default method phiToiThieu()/phiToiDa(): co san nhung override duoc
 *
 * 5. THEM:
 *    - Enum nang cao: field, constructor, abstract method (LoaiGiaoDich)
 *    - Record (GiaoDich): immutable value object
 *    - Sealed class + pattern matching (SuKienGiaoDich)
 *    - Composition vs Inheritance (ChinhSachPhiVip has-a ChinhSachPhi)
 *    - Generics (Comparator<GiaoDich>, Predicate<GiaoDich>)
 *    - Lambda / Method reference / Anonymous class (ThongKeGiaoDich)
 *    - Functional interface + andThen (GiaoDichListener)
 *    - Static vs Instance (GiaoDichProcessor.sinhMaGiaoDich vs instance method)
 *    - final method / final class / final field
 *    - Inner static class (SuKienGiaoDich.GiaoDichThanhCong)
 *    - Access modifier: private/protected/public/package-private
 */
public class OopDemo {

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("  DEMO OOP JAVA — HE THONG TINH PHI GIAO DICH CHUNG KHOAN");
        System.out.println("=".repeat(70));

        // -----------------------------------------------------------------
        // 1. POLYMORPHISM qua INTERFACE: cung 1 phep tinh, 3 ket qua khac nhau
        // -----------------------------------------------------------------
        System.out.println("\n--- 1. POLYMORPHISM: Cung giao dich MUA 50 trieu, 3 chinh sach phi ---");

        ChinhSachPhi phiCoSo = new ChinhSachPhiCoSo();
        ChinhSachPhi phiKyQuy = new ChinhSachPhiKyQuy();
        ChinhSachPhi phiVip = new ChinhSachPhiVip();

        double soTien = 50_000_000;
        LoaiGiaoDich loai = LoaiGiaoDich.MUA;

        // Cung goi tinhPhiThucTe() tren interface — JVM tu chon dung implementation
        System.out.printf("  Co so:  %,.0f dong%n", phiCoSo.tinhPhiThucTe(loai, soTien));
        System.out.printf("  Ky quy: %,.0f dong%n", phiKyQuy.tinhPhiThucTe(loai, soTien));
        System.out.printf("  VIP:    %,.0f dong%n", phiVip.tinhPhiThucTe(loai, soTien));

        // -----------------------------------------------------------------
        // 2. ENUM POLYMORPHISM: cung soTien, moi LoaiGiaoDich tinh phi khac
        // -----------------------------------------------------------------
        System.out.println("\n--- 2. ENUM POLYMORPHISM: Phi co so cho 50 trieu, theo loai giao dich ---");

        for (LoaiGiaoDich ld : LoaiGiaoDich.values()) {
            System.out.printf("  %s: %,.0f dong%n", ld.getMoTa(), ld.tinhPhiCoSo(soTien));
        }

        // -----------------------------------------------------------------
        // 3. COMPOSITION + doi chinh sach luc runtime
        // -----------------------------------------------------------------
        System.out.println("\n--- 3. COMPOSITION: Doi chinh sach phi runtime (nang cap VIP) ---");

        GiaoDichProcessor processor = new GiaoDichProcessor("TK000001", phiCoSo);
        System.out.println("  Truoc: " + processor.getChinhSachPhi());

        // Thuc hien giao dich voi phi co so
        SuKienGiaoDich event1 = processor.thucHienGiaoDich(LoaiGiaoDich.MUA, 30_000_000);
        System.out.println("  " + event1.moTa());

        // Nang cap VIP — doi reference sang chinh sach khac (Polymorphism runtime)
        processor.setChinhSachPhi(phiVip);
        System.out.println("  Sau nang cap: " + processor.getChinhSachPhi());

        // Cung giao dich, phi giam dang ke
        SuKienGiaoDich event2 = processor.thucHienGiaoDich(LoaiGiaoDich.MUA, 30_000_000);
        System.out.println("  " + event2.moTa());

        // -----------------------------------------------------------------
        // 4. SEALED CLASS + PATTERN MATCHING
        // -----------------------------------------------------------------
        System.out.println("\n--- 4. SEALED CLASS + PATTERN MATCHING ---");

        SuKienGiaoDich eventFail = new SuKienGiaoDich.GiaoDichThatBai(
                "TK000002", LoaiGiaoDich.BAN, "So du khong du");
        SuKienGiaoDich eventPending = new SuKienGiaoDich.GiaoDichChoXuLy(
                "TK000003", LoaiGiaoDich.CHUYEN_KHOAN, 200_000_000);

        List<SuKienGiaoDich> events = List.of(event1, event2, eventFail, eventPending);
        for (SuKienGiaoDich ev : events) {
            System.out.println("  " + SuKienGiaoDich.xuLySuKien(ev));
        }

        // -----------------------------------------------------------------
        // 5. FUNCTIONAL INTERFACE + LAMBDA + ANONYMOUS CLASS
        // -----------------------------------------------------------------
        System.out.println("\n--- 5. LAMBDA vs ANONYMOUS CLASS (GiaoDichListener) ---");

        // Lambda — gon:
        GiaoDichListener logListener = (suKien) ->
                System.out.println("    [LOG] " + suKien.moTa());

        // Anonymous class — tuong duong nhung verbose hon:
        GiaoDichListener alertListener = new GiaoDichListener() {
            @Override
            public void onSuKien(SuKienGiaoDich suKien) {
                if (suKien instanceof SuKienGiaoDich.GiaoDichThatBai tb) {
                    System.out.println("    [ALERT] Giao dich that bai: " + tb.getLyDo());
                }
            }
        };

        // Chain 2 listener bang default method andThen():
        GiaoDichListener combined = logListener.andThen(alertListener);
        System.out.println("  Goi combined listener:");
        combined.onSuKien(event1);
        combined.onSuKien(eventFail);

        // -----------------------------------------------------------------
        // 6. RECORD (immutable) + COMPARATOR + STREAM
        // -----------------------------------------------------------------
        System.out.println("\n--- 6. RECORD + COMPARATOR + STREAM (ThongKeGiaoDich) ---");

        // Tao 1 so giao dich mau
        List<GiaoDich> mau = List.of(
                new GiaoDich("GD001", "TK01", LoaiGiaoDich.MUA, 20_000_000, 30_000, LocalDateTime.now(), "Mua A"),
                new GiaoDich("GD002", "TK01", LoaiGiaoDich.BAN, 150_000_000, 375_000, LocalDateTime.now(), "Ban B"),
                new GiaoDich("GD003", "TK01", LoaiGiaoDich.MUA, 5_000_000, 10_000, LocalDateTime.now(), "Mua C"),
                new GiaoDich("GD004", "TK02", LoaiGiaoDich.CHUYEN_KHOAN, 80_000_000, 50_000, LocalDateTime.now(), "Chuyen D")
        );

        ThongKeGiaoDich thongKe = new ThongKeGiaoDich(mau);

        System.out.printf("  Tong gia tri: %,.0f dong%n", thongKe.tongGiaTriGiaoDich());
        System.out.printf("  Tong phi:     %,.0f dong%n", thongKe.tongPhiGiaoDich());
        System.out.printf("  Phi trung binh: %,.0f dong%n", thongKe.phiTrungBinh());
        System.out.printf("  So giao dich lon (>= 100 trieu): %d%n", thongKe.locGiaoDichLon().size());

        // Loc bang lambda (Predicate):
        List<GiaoDich> gdMua = thongKe.locTheoLoai(LoaiGiaoDich.MUA);
        System.out.printf("  So giao dich MUA: %d%n", gdMua.size());

        // Sap xep bang Comparator:
        List<GiaoDich> sorted = thongKe.sapXep(ThongKeGiaoDich.THEO_SO_TIEN_GIAM);
        System.out.println("  Top giao dich theo gia tri giam dan:");
        for (GiaoDich gd : sorted) {
            System.out.printf("    %s - %s: %,.0f%n", gd.maGiaoDich(), gd.loaiGiaoDich().getMoTa(), gd.soTien());
        }

        // -----------------------------------------------------------------
        // 7. VIP — COMPOSITION demo soTienTietKiem
        // -----------------------------------------------------------------
        System.out.println("\n--- 7. COMPOSITION: VIP tiet kiem bao nhieu so voi Co so? ---");

        ChinhSachPhiVip vip = new ChinhSachPhiVip(phiCoSo); // has-a ChinhSachPhiCoSo
        double giaTriLon = 200_000_000;
        for (LoaiGiaoDich ld : LoaiGiaoDich.values()) {
            double tietKiem = vip.soTienTietKiem(ld, giaTriLon);
            System.out.printf("  %s 200 trieu → VIP tiet kiem: %,.0f dong%n",
                    ld.getMoTa(), tietKiem);
        }

        // -----------------------------------------------------------------
        // 8. INHERITANCE CHAIN — in toString() cua tung chinh sach
        // -----------------------------------------------------------------
        System.out.println("\n--- 8. INHERITANCE + toString() override ---");

        List<ChinhSachPhi> tatCa = List.of(phiCoSo, phiKyQuy, phiVip);
        for (ChinhSachPhi cs : tatCa) {
            // toString() duoc Object dinh nghia, AbstractChinhSachPhi override,
            // tat ca subclass ke thua ban override do — KHONG can viet lai.
            System.out.println("  " + cs);
        }

        // -----------------------------------------------------------------
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  KET THUC DEMO — Xem comment trong code de hieu chi tiet tung khai niem");
        System.out.println("=".repeat(70));
    }
}
