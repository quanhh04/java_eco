package com.demo.securities.oop;

/**
 * === ABSTRACT CLASS ===
 * Khac interface: abstract class CO THE co state (field), constructor, method co than.
 * Khac concrete class: KHONG the tao instance truc tiep (new AbstractChinhSachPhi() → loi).
 *
 * Dung khi:
 * - Co logic CHUNG giua cac implementation (DRY — khong lap lai code)
 * - Can field instance (state) chia se
 * - Van muon bat subclass phai implement 1 so method (abstract)
 *
 * === SO SANH INTERFACE vs ABSTRACT CLASS ===
 * | Interface                        | Abstract Class                    |
 * |----------------------------------|-----------------------------------|
 * | Khong co state (field instance)  | Co state                          |
 * | 1 class implement NHIEU          | 1 class chi extend 1 (don ke thua)|
 * | Default method (tu Java 8)       | Method co than binh thuong         |
 * | Khong co constructor             | Co constructor (subclass goi super)|
 * | "Can do what" (capability)       | "Is a" (ban chat)                 |
 *
 * Class nay lam "template" cho cac chinh sach phi cu the.
 */
public abstract class AbstractChinhSachPhi implements ChinhSachPhi {

    // === ENCAPSULATION: field private, chi doc qua getter ===
    private final String tenChinhSach;
    private final double tyLeGiamPhi; // 0.0 = khong giam, 0.3 = giam 30%

    /**
     * === CONSTRUCTOR cua ABSTRACT CLASS ===
     * Khong the goi truc tiep (new AbstractChinhSachPhi(...) → loi).
     * Chi duoc goi qua super(...) tu constructor cua subclass.
     * => Dam bao subclass PHAI cung cap thong tin nay.
     *
     * === PROTECTED ===
     * protected: chi class trong cung package HOAC subclass moi truy cap duoc.
     * Dung cho constructor/method ma chi subclass nen goi.
     */
    protected AbstractChinhSachPhi(String tenChinhSach, double tyLeGiamPhi) {
        if (tenChinhSach == null || tenChinhSach.isBlank()) {
            throw new IllegalArgumentException("Ten chinh sach khong duoc trong");
        }
        if (tyLeGiamPhi < 0 || tyLeGiamPhi >= 1) {
            throw new IllegalArgumentException("Ty le giam phi phai trong [0, 1): " + tyLeGiamPhi);
        }
        this.tenChinhSach = tenChinhSach;
        this.tyLeGiamPhi = tyLeGiamPhi;
    }

    // === FINAL method: subclass KHONG duoc override ===
    // Ten chinh sach da dat luc tao, khong ai duoc doi logic lay ten.
    @Override
    public final String tenChinhSach() {
        return tenChinhSach;
    }

    /**
     * === TEMPLATE METHOD PATTERN ===
     * Dinh nghia "khung" tinh phi: goi tinhPhiCoSo() (abstract, subclass quyet dinh)
     * roi ap dung ty le giam phi (logic chung, khong lap lai o moi subclass).
     *
     * Flow: tinhPhi() [final, khong override] → tinhPhiCoSo() [abstract, subclass quyet dinh]
     *       → ap giam phi → return.
     */
    @Override
    public final double tinhPhi(LoaiGiaoDich loaiGiaoDich, double soTien) {
        double phiGoc = tinhPhiCoSo(loaiGiaoDich, soTien);
        return phiGoc * (1 - tyLeGiamPhi);
    }

    /**
     * === ABSTRACT METHOD ===
     * Khong co than — subclass BAT BUOC phai override.
     * Moi loai tai khoan (co so, ky quy, VIP) se tinh phi goc khac nhau.
     */
    protected abstract double tinhPhiCoSo(LoaiGiaoDich loaiGiaoDich, double soTien);

    /**
     * Getter — encapsulation: chi doc, khong sua.
     */
    public double getTyLeGiamPhi() {
        return tyLeGiamPhi;
    }

    /**
     * === toString() override tu Object ===
     * Moi class Java deu ke thua Object (ke thua ngam dinh).
     * Override toString() de in thong tin co nghia thay vi dia chi bo nho.
     */
    @Override
    public String toString() {
        return tenChinhSach + " (giam " + (int) (tyLeGiamPhi * 100) + "% phi)";
    }
}
