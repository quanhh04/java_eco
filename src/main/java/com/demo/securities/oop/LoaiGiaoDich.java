package com.demo.securities.oop;

/**
 * === ENUM NANG CAO ===
 * Enum trong Java khong chi la hang so — moi gia tri co the co field, method, constructor.
 * Enum nay minh hoa:
 * - Field instance (moTa, heSoPhiMacDinh)
 * - Constructor (private — bat buoc)
 * - Method thuong (getMoTa(), getHeSoPhiMacDinh())
 * - Abstract method trong enum (tinhPhiCoSo) — moi gia tri PHAI override
 *   => day chinh la POLYMORPHISM tren enum: cung goi tinhPhiCoSo() nhung moi
 *      gia tri tinh khac nhau.
 */
public enum LoaiGiaoDich {

    MUA("Mua chung khoan", 0.0015) {
        /**
         * Mua: phi = soTien * heSo (ty le co dinh)
         */
        @Override
        public double tinhPhiCoSo(double soTien) {
            return soTien * getHeSoPhiMacDinh();
        }
    },

    BAN("Ban chung khoan", 0.0015) {
        /**
         * Ban: phi = soTien * heSo + thue ban (0.1% thue tren gia ban)
         */
        @Override
        public double tinhPhiCoSo(double soTien) {
            double phiSan = soTien * getHeSoPhiMacDinh();
            double thueBan = soTien * 0.001; // 0.1% thue ban chung khoan
            return phiSan + thueBan;
        }
    },

    CHUYEN_KHOAN("Chuyen tien giua tai khoan", 0.0005) {
        /**
         * Chuyen khoan: phi co dinh 10,000 + ty le nho
         */
        @Override
        public double tinhPhiCoSo(double soTien) {
            return 10_000 + soTien * getHeSoPhiMacDinh();
        }
    };

    // === Field instance cua enum ===
    private final String moTa;
    private final double heSoPhiMacDinh;

    // === Constructor (luon private trong enum) ===
    LoaiGiaoDich(String moTa, double heSoPhiMacDinh) {
        this.moTa = moTa;
        this.heSoPhiMacDinh = heSoPhiMacDinh;
    }

    public String getMoTa() {
        return moTa;
    }

    public double getHeSoPhiMacDinh() {
        return heSoPhiMacDinh;
    }

    /**
     * === ABSTRACT METHOD TRONG ENUM ===
     * Moi gia tri enum PHAI implement — dam bao khong ai quen.
     * Day la Polymorphism: MUA.tinhPhiCoSo(x) khac BAN.tinhPhiCoSo(x).
     */
    public abstract double tinhPhiCoSo(double soTien);
}
