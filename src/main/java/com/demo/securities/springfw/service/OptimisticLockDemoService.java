package com.demo.securities.springfw.service;

import com.demo.securities.exception.NotFoundException;
import com.demo.securities.springfw.entity.TaiKhoanEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

/**
 * Demo Optimistic Lock deterministic (không cần 2 thread/race timing thật, luôn
 * tái hiện đúng 1 kịch bản) — dùng 2 EntityManager riêng biệt trong cùng 1 request
 * để mô phỏng "2 người dùng khác nhau cùng sửa 1 tài khoản":
 *
 *   Phiên A (do @Transactional của Spring quản lý, EntityManager tiêm qua @PersistenceContext):
 *   đọc tài khoản, GIỮ NGUYÊN trong bộ nhớ (version lúc đọc, vd version=5).
 *
 *   Phiên B (tự tạo EntityManager + transaction riêng qua EntityManagerFactory,
 *   KHÔNG liên quan gì tới transaction của Spring): đọc lại CÙNG tài khoản, sửa,
 *   commit NGAY — giống 1 request khác đã "thắng" trước, DB giờ có version=6.
 *
 *   Phiên A sửa tiếp trên entity đang giữ (vẫn version=5 trong bộ nhớ) — khi
 *   @Transactional của Spring commit lúc method return, Hibernate so version=5
 *   (trong bộ nhớ) với version=6 (DB thật) → lệch → ném OptimisticLockException,
 *   KHÔNG âm thầm ghi đè lên thay đổi của phiên B (lost update).
 */
public class OptimisticLockDemoService {

    @PersistenceContext
    private EntityManager entityManager;

    private final EntityManagerFactory entityManagerFactory;

    public OptimisticLockDemoService(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Transactional
    public void demoXungDot(String soTaiKhoan, double soTienPhienA, double soTienPhienB) {
        TaiKhoanEntity phienA = entityManager.find(TaiKhoanEntity.class, soTaiKhoan);
        if (phienA == null) {
            throw new NotFoundException("Khong tim thay tai khoan: " + soTaiKhoan);
        }

        EntityManager emPhienB = entityManagerFactory.createEntityManager();
        try {
            emPhienB.getTransaction().begin();
            TaiKhoanEntity phienB = emPhienB.find(TaiKhoanEntity.class, soTaiKhoan);
            phienB.setSoDuTien(phienB.getSoDuTien() + soTienPhienB);
            emPhienB.getTransaction().commit();
        } finally {
            emPhienB.close();
        }

        // Phien A van dang sua tren entity CU (version chua doi trong bo nho) - KHONG
        // tu flush() o day. De Hibernate tu phat hien luc @Transactional cua Spring
        // commit sau khi method nay return - dung y het tinh huong that: loi xay ra
        // "am tham" o tang ha tang, khong phai do goi flush() tuong minh trong code nghiep vu.
        phienA.setSoDuTien(phienA.getSoDuTien() + soTienPhienA);
    }
}
