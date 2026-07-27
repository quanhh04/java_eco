package com.demo.securities.oop;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * === GENERICS, COMPARABLE, COMPARATOR, FUNCTIONAL INTERFACE, LAMBDA ===
 * Class nay tong hop nhieu khai niem OOP/Java nang cao qua nghiep vu
 * thong ke giao dich.
 */
public class ThongKeGiaoDich {

    private final List<GiaoDich> danhSach;

    public ThongKeGiaoDich(List<GiaoDich> danhSach) {
        this.danhSach = List.copyOf(danhSach); // immutable copy — encapsulation
    }

    // =======================================================================
    // === COMPARABLE vs COMPARATOR ===
    // Comparable: doi tuong TU BIET cach so sanh voi doi tuong khac (natural order)
    //             → implement Comparable<T> ngay trong class
    // Comparator: logic so sanh TACH RIENG — co the co nhieu Comparator khac nhau
    //             cho cung 1 class, truyen vao sort() tuy context.
    // =======================================================================

    /**
     * === COMPARATOR (tach rieng logic so sanh) ===
     * Tao Comparator bang static method reference — gon hon anonymous class.
     */
    public static final Comparator<GiaoDich> THEO_SO_TIEN =
            Comparator.comparingDouble(GiaoDich::soTien);

    public static final Comparator<GiaoDich> THEO_SO_TIEN_GIAM =
            Comparator.comparingDouble(GiaoDich::soTien).reversed();

    public static final Comparator<GiaoDich> THEO_THOI_GIAN =
            Comparator.comparing(GiaoDich::thoiGian);

    /** Comparator phuc tap: theo loai truoc, theo so tien sau */
    public static final Comparator<GiaoDich> THEO_LOAI_ROI_SO_TIEN =
            Comparator.comparing(GiaoDich::loaiGiaoDich)
                    .thenComparingDouble(GiaoDich::soTien);

    // =======================================================================
    // === LAMBDA & FUNCTIONAL INTERFACE ===
    // Predicate<T>: interface co 1 method test(T) → true/false.
    // Dung lambda (arrow function) thay cho anonymous class:
    //   (gd) -> gd.soTien() > 1_000_000
    // tuong duong:
    //   new Predicate<GiaoDich>() { boolean test(GiaoDich gd) { return gd.soTien() > 1_000_000; } }
    // =======================================================================

    /**
     * Loc giao dich theo dieu kien bat ky (Predicate).
     * Nguoi goi tu dinh nghia dieu kien bang lambda.
     *
     * === GENERIC METHOD ===
     * Khong phai class generic, nhung dung Predicate<GiaoDich> la generic interface.
     */
    public List<GiaoDich> loc(Predicate<GiaoDich> dieuKien) {
        return danhSach.stream()
                .filter(dieuKien)
                .toList();
    }

    /** Loc theo loai giao dich */
    public List<GiaoDich> locTheoLoai(LoaiGiaoDich loai) {
        // Lambda: (gd) -> gd.loaiGiaoDich() == loai
        return loc(gd -> gd.loaiGiaoDich() == loai);
    }

    /** Loc giao dich lon (>= 100 trieu) — dung method reference */
    public List<GiaoDich> locGiaoDichLon() {
        // Method reference: GiaoDich::isGiaoDichLon tuong duong (gd) -> gd.isGiaoDichLon()
        return loc(GiaoDich::isGiaoDichLon);
    }

    /** Sap xep theo Comparator truyen vao */
    public List<GiaoDich> sapXep(Comparator<GiaoDich> comparator) {
        return danhSach.stream()
                .sorted(comparator)
                .toList();
    }

    // =======================================================================
    // === ANONYMOUS CLASS vs LAMBDA ===
    // =======================================================================

    /**
     * Demo: tao Comparator bang ANONYMOUS CLASS (cach cu, pre-Java 8).
     * Anonymous class: class khong ten, dinh nghia ngay tai cho.
     */
    public List<GiaoDich> sapXepPhiGiamDan_AnonymousClass() {
        // Anonymous class — verbose nhung ro rang voi nguoi moi hoc:
        Comparator<GiaoDich> comp = new Comparator<GiaoDich>() {
            @Override
            public int compare(GiaoDich a, GiaoDich b) {
                return Double.compare(b.phiGiaoDich(), a.phiGiaoDich()); // giam dan
            }
        };
        return danhSach.stream().sorted(comp).toList();
    }

    /**
     * Cung logic, nhung dung LAMBDA (Java 8+) — gon hon nhieu.
     */
    public List<GiaoDich> sapXepPhiGiamDan_Lambda() {
        return danhSach.stream()
                .sorted((a, b) -> Double.compare(b.phiGiaoDich(), a.phiGiaoDich()))
                .toList();
    }

    // =======================================================================
    // === THONG KE (dung stream — khong phai OOP nhung hay di kem) ===
    // =======================================================================

    public double tongPhiGiaoDich() {
        return danhSach.stream().mapToDouble(GiaoDich::phiGiaoDich).sum();
    }

    public double tongGiaTriGiaoDich() {
        return danhSach.stream().mapToDouble(GiaoDich::soTien).sum();
    }

    public long soLuong() {
        return danhSach.size();
    }

    public double phiTrungBinh() {
        return danhSach.isEmpty() ? 0 : tongPhiGiaoDich() / soLuong();
    }
}
