package com.demo.securities.oop;

/**
 * === KE THUA — CHINH SACH PHI KY QUY ===
 * Tai khoan ky quy (margin): phi cao hon co so vi rui ro vay margin.
 * - He so phi nhan 1.5 (phat sinh phi vay margin)
 * - Giam phi 10% (khuyen khich giao dich nhieu)
 * - San phi 15,000, tran phi 100 trieu
 *
 * Minh hoa: cung ke thua AbstractChinhSachPhi nhung logic tinhPhiCoSo() KHAC
 * hoàn toàn so voi ChinhSachPhiCoSo — day la POLYMORPHISM.
 */
public class ChinhSachPhiKyQuy extends AbstractChinhSachPhi {

    /** He so nhan them cho phi margin (vay de giao dich) */
    private static final double HE_SO_MARGIN = 1.5;

    public ChinhSachPhiKyQuy() {
        super("Phi tai khoan Ky quy (Margin)", 0.10); // giam 10% phi
    }

    /**
     * Phi ky quy = phi co ban * HE_SO_MARGIN (vi co rui ro margin).
     */
    @Override
    protected double tinhPhiCoSo(LoaiGiaoDich loaiGiaoDich, double soTien) {
        return loaiGiaoDich.tinhPhiCoSo(soTien) * HE_SO_MARGIN;
    }

    @Override
    public double phiToiThieu() {
        return 15_000; // san phi cao hon tai khoan co so
    }

    @Override
    public double phiToiDa() {
        return 100_000_000;
    }
}
