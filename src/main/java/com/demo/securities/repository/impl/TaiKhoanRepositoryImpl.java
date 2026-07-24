package com.demo.securities.repository.impl;

import com.demo.securities.config.DatabaseConfig;
import com.demo.securities.exception.DataAccessException;
import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.model.TrangThaiTaiKhoan;
import com.demo.securities.repository.TaiKhoanRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaiKhoanRepositoryImpl implements TaiKhoanRepository {

    private final String table = DatabaseConfig.getSchema() + ".tai_khoan_chung_khoan";

    @Override
    public List<TaiKhoanChungKhoan> findAll() {
        String sql = "SELECT * FROM " + table + " ORDER BY so_tai_khoan";
        List<TaiKhoanChungKhoan> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Không lấy được danh sách tài khoản", e);
        }
    }

    @Override
    public Optional<TaiKhoanChungKhoan> findBySoTaiKhoan(String soTaiKhoan) {
        String sql = "SELECT * FROM " + table + " WHERE so_tai_khoan = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soTaiKhoan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không truy vấn được tài khoản", e);
        }
    }

    @Override
    public List<TaiKhoanChungKhoan> findByKhachHangId(String khachHangId) {
        String sql = "SELECT * FROM " + table + " WHERE khach_hang_id = ? ORDER BY so_tai_khoan";
        List<TaiKhoanChungKhoan> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Không lấy được tài khoản theo khách hàng", e);
        }
    }

    @Override
    public void save(TaiKhoanChungKhoan taiKhoan) {
        String sql = "INSERT INTO " + table +
                " (so_tai_khoan, khach_hang_id, loai_tai_khoan, trang_thai, ngay_mo, so_du_tien)" +
                " VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, taiKhoan.getSoTaiKhoan());
            ps.setString(2, taiKhoan.getKhachHangId());
            ps.setString(3, taiKhoan.getLoaiTaiKhoan().name());
            ps.setString(4, taiKhoan.getTrangThai().name());
            ps.setTimestamp(5, Timestamp.valueOf(taiKhoan.getNgayMo()));
            ps.setBigDecimal(6, java.math.BigDecimal.valueOf(taiKhoan.getSoDuTien()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Không mở được tài khoản", e);
        }
    }

    @Override
    public void update(TaiKhoanChungKhoan taiKhoan) {
        String sql = "UPDATE " + table + " SET trang_thai = ?, so_du_tien = ? WHERE so_tai_khoan = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, taiKhoan.getTrangThai().name());
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(taiKhoan.getSoDuTien()));
            ps.setString(3, taiKhoan.getSoTaiKhoan());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Không cập nhật được tài khoản", e);
        }
    }

    private TaiKhoanChungKhoan map(ResultSet rs) throws SQLException {
        return new TaiKhoanChungKhoan(
                rs.getString("so_tai_khoan"),
                rs.getString("khach_hang_id"),
                LoaiTaiKhoan.valueOf(rs.getString("loai_tai_khoan")),
                TrangThaiTaiKhoan.valueOf(rs.getString("trang_thai")),
                rs.getTimestamp("ngay_mo").toLocalDateTime(),
                rs.getBigDecimal("so_du_tien").doubleValue()
        );
    }
}
