package com.demo.securities.oop;

/**
 * === INTERFACE (ABSTRACTION) ===
 * Interface la "hop dong" — dinh nghia METHOD phai co, KHONG dinh nghia cach lam.
 * Bat ky class nao implement deu PHAI cung cap than method.
 *
 * Tai sao dung interface thay vi class?
 * - 1 class co the implement NHIEU interface (Java khong ho tro da ke thua class)
 * - Tach "lam gi" khoi "lam nhu the nao" — code goi chi biet interface, khong
 *   quan tam implementation cu the (Dependency Inversion)
 * - De test (mock duoc)
 *
 * === DEFAULT METHOD (Java 8+) ===
 * Interface co the co method co than (default) — cho code chung ma khong bat
 * tat ca implementation phai viet lai. Khac abstract class: interface khong co state.
 */
public interface ChinhSachPhi {

    /**
     * Tinh phi cho 1 giao dich. Moi chinh sach khac nhau se tinh khac nhau.
     * Day la diem Polymorphism chinh: cung goi tinhPhi() nhung ket qua khac.
     *
     * @param loaiGiaoDich loai giao dich (mua/ban/chuyen)
     * @param soTien gia tri giao dich
     * @return so tien phi
     */
    double tinhPhi(LoaiGiaoDich loaiGiaoDich, double soTien);

    /**
     * Ten hien thi cua chinh sach phi.
     */
    String tenChinhSach();

    /**
     * Muc phi toi thieu (san phi). Mac dinh 10,000 dong.
     * Implementation co the override neu muon thay doi.
     *
     * === DEFAULT METHOD ===
     * Cung cap gia tri mac dinh — class implement KHONG bat buoc override,
     * nhung CO THE override neu can (VIP co the set ve 0).
     */
    default double phiToiThieu() {
        return 10_000;
    }

    /**
     * Muc phi toi da (tran phi). Mac dinh khong gioi han (Double.MAX_VALUE).
     */
    default double phiToiDa() {
        return Double.MAX_VALUE;
    }

    /**
     * Tinh phi co ap dung san + tran.
     * Day la "template" nam ngay trong interface — goi tinhPhi() (abstract)
     * roi clamp vao [phiToiThieu, phiToiDa]. Ca 3 method co the override rieng.
     */
    default double tinhPhiThucTe(LoaiGiaoDich loaiGiaoDich, double soTien) {
        double phi = tinhPhi(loaiGiaoDich, soTien);
        phi = Math.max(phi, phiToiThieu());
        phi = Math.min(phi, phiToiDa());
        return phi;
    }
}
