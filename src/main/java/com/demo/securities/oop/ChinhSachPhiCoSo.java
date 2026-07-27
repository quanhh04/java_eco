package com.demo.securities.oop;

/**
 * === CONCRETE CLASS — KE THUA (INHERITANCE) ===
 * Ke thua AbstractChinhSachPhi: duoc thua huong tenChinhSach, tyLeGiamPhi,
 * tinhPhi() (template), toString() — chi can viet PHAN KHAC BIET (tinhPhiCoSo).
 *
 * Day la chinh sach phi cho tai khoan CO SO (loai thuong nhat):
 * - Phi = phi mac dinh theo loai giao dich (LoaiGiaoDich.tinhPhiCoSo())
 * - Khong giam phi (tyLeGiamPhi = 0)
 * - San phi 10,000, tran phi 50 trieu
 */
public class ChinhSachPhiCoSo extends AbstractChinhSachPhi {

    /**
     * === CONSTRUCTOR GOI super() ===
     * Subclass PHAI goi constructor cha (super) — cung cap thong tin bat buoc.
     * Neu cha khong co constructor mac dinh (no-arg), PHAI goi tuong minh.
     */
    public ChinhSachPhiCoSo() {
        super("Phi tai khoan Co so", 0.0); // khong giam phi
    }

    /**
     * === OVERRIDE — POLYMORPHISM ===
     * Cung ten method (tinhPhiCoSo), nhung class nay tinh khac class khac.
     * @Override annotation: compiler kiem tra rang method nay THAT SU override
     * method cua cha — neu viet sai ten/tham so, compiler bao loi ngay.
     */
    @Override
    protected double tinhPhiCoSo(LoaiGiaoDich loaiGiaoDich, double soTien) {
        // Delegate cho enum LoaiGiaoDich — moi loai giao dich tu biet cach tinh phi goc
        return loaiGiaoDich.tinhPhiCoSo(soTien);
    }

    /**
     * Override phiToiDa() tu interface (qua default method).
     * Tai khoan co so: tran phi 50 trieu.
     */
    @Override
    public double phiToiDa() {
        return 50_000_000;
    }
}
