package com.demo.securities.repository;

import com.demo.securities.model.TaiKhoanChungKhoan;

import java.util.List;
import java.util.Optional;

public interface TaiKhoanRepository {

    List<TaiKhoanChungKhoan> findAll();

    Optional<TaiKhoanChungKhoan> findBySoTaiKhoan(String soTaiKhoan);

    List<TaiKhoanChungKhoan> findByKhachHangId(String khachHangId);

    void save(TaiKhoanChungKhoan taiKhoan);

    void update(TaiKhoanChungKhoan taiKhoan);
}
