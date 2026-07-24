package com.demo.securities.springfw.dao;

import com.demo.securities.springfw.entity.TaiKhoanEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 * Dùng EntityManager trực tiếp (không dùng Spring Data JPA) — giữ đúng tinh thần
 * "thấy bản chất Hibernate hoạt động thế nào" thay vì thêm 1 lớp magic khác đè lên.
 */
public class TaiKhoanDao {

    @PersistenceContext
    private EntityManager entityManager;

    public TaiKhoanEntity find(String soTaiKhoan) {
        return entityManager.find(TaiKhoanEntity.class, soTaiKhoan);
    }

    public List<TaiKhoanEntity> findAll() {
        return entityManager.createQuery("SELECT t FROM TaiKhoanEntity t ORDER BY t.soTaiKhoan", TaiKhoanEntity.class)
                .getResultList();
    }

    public List<TaiKhoanEntity> findByKhachHangId(String khachHangId) {
        return entityManager.createQuery(
                        "SELECT t FROM TaiKhoanEntity t WHERE t.khachHangId = :khachHangId ORDER BY t.soTaiKhoan",
                        TaiKhoanEntity.class)
                .setParameter("khachHangId", khachHangId)
                .getResultList();
    }

    public List<String> findAllSoTaiKhoan() {
        return entityManager.createQuery("SELECT t.soTaiKhoan FROM TaiKhoanEntity t", String.class)
                .getResultList();
    }

    public void save(TaiKhoanEntity taiKhoan) {
        entityManager.persist(taiKhoan);
    }
}
