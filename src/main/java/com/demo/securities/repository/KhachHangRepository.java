package com.demo.securities.repository;

import com.demo.securities.model.KhachHang;

import java.util.List;
import java.util.Optional;

public interface KhachHangRepository {

    List<KhachHang> findAll();

    Optional<KhachHang> findById(String id);

    Optional<KhachHang> findByCCCD(String soCCCD);

    List<KhachHang> searchByTen(String keyword);

    void save(KhachHang khachHang);

    void update(KhachHang khachHang);

    boolean deleteById(String id);
}
