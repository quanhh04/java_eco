package com.demo.securities.oop;

/**
 * === FUNCTIONAL INTERFACE (Java 8+) ===
 * Interface chi co DUNG 1 abstract method → co the dung LAMBDA thay cho
 * anonymous class khi implement.
 *
 * @FunctionalInterface: annotation nay BAT compiler bao loi neu ai them
 * method abstract thu 2 vao interface (bao ve tinh functional).
 *
 * Pattern "Observer/Listener": GiaoDichProcessor co the thong bao cho nhieu
 * listener khi giao dich xay ra — moi listener xu ly khac nhau (ghi log,
 * gui email, cap nhat dashboard...) → Polymorphism qua callback.
 */
@FunctionalInterface
public interface GiaoDichListener {

    /**
     * Duoc goi khi co su kien giao dich xay ra.
     */
    void onSuKien(SuKienGiaoDich suKien);

    // === DEFAULT METHOD trong functional interface — KHONG tinh la abstract method ===
    // Van la functional interface vi chi co 1 abstract (onSuKien).

    /**
     * Chain 2 listener: listener nay chay truoc, listener kia chay sau.
     * Pattern "andThen" — giong Function.andThen() trong java.util.function.
     */
    default GiaoDichListener andThen(GiaoDichListener next) {
        // "this" o day la lambda/instance hien tai
        return (suKien) -> {
            this.onSuKien(suKien);
            next.onSuKien(suKien);
        };
    }
}
