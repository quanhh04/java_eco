package com.demo.securities.oop;

/**
 * === KE THUA — CHINH SACH PHI VIP ===
 * Tai khoan VIP (khach hang lon): giam phi manh, tran phi thap.
 *
 * Minh hoa them:
 * - COMPOSITION: ChinhSachPhiVip CO 1 ChinhSachPhi lam "base" de so sanh
 *   (giu reference den 1 chinh sach khac, khong phai ke thua no).
 *   => "Has-a" (composition) thay vi "Is-a" (inheritance).
 * - this vs super: dung this truy cap field/method cua chinh minh,
 *   super truy cap method cua class cha.
 */
public class ChinhSachPhiVip extends AbstractChinhSachPhi {

    /**
     * === COMPOSITION (Has-a) ===
     * VIP giu reference den 1 chinh sach "co so" de co the so sanh phi
     * hoac tinh phi co so theo logic khac — KHONG ke thua ChinhSachPhiCoSo,
     * chi "so huu" no nhu 1 tool.
     *
     * Composition vs Inheritance:
     * - Inheritance: "ChinhSachPhiVip IS-A ChinhSachPhiCoSo" (khong dung nghia)
     * - Composition: "ChinhSachPhiVip HAS-A reference den ChinhSachPhiCoSo de so sanh"
     */
    private final ChinhSachPhi chinhSachThamChieu;

    /**
     * Constructor voi chinhSachThamChieu — de VIP co the "biet" phi binh thuong
     * la bao nhieu, tu do tinh % tiet kiem cho khach.
     */
    public ChinhSachPhiVip(ChinhSachPhi chinhSachThamChieu) {
        super("Phi tai khoan VIP", 0.40); // giam 40% phi
        this.chinhSachThamChieu = chinhSachThamChieu;
    }

    /** Constructor mac dinh: tham chieu den co so */
    public ChinhSachPhiVip() {
        this(new ChinhSachPhiCoSo());
    }

    @Override
    protected double tinhPhiCoSo(LoaiGiaoDich loaiGiaoDich, double soTien) {
        // VIP tinh phi goc giong co so (LoaiGiaoDich quyet dinh)
        return loaiGiaoDich.tinhPhiCoSo(soTien);
    }

    @Override
    public double phiToiThieu() {
        return 0; // VIP khong co san phi
    }

    @Override
    public double phiToiDa() {
        return 20_000_000; // tran phi thap hon co so
    }

    /**
     * Method RIENG cua VIP (khong co trong cha/interface):
     * Tinh so tien khach TIET KIEM duoc so voi chinh sach tham chieu.
     *
     * === this keyword ===
     * this.tinhPhiThucTe() — goi method cua CHINH minh (ChinhSachPhiVip instance).
     */
    public double soTienTietKiem(LoaiGiaoDich loaiGiaoDich, double soTien) {
        double phiBinhThuong = chinhSachThamChieu.tinhPhiThucTe(loaiGiaoDich, soTien);
        double phiVip = this.tinhPhiThucTe(loaiGiaoDich, soTien);
        return phiBinhThuong - phiVip;
    }
}
