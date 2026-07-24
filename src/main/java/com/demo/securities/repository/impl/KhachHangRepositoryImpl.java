package com.demo.securities.repository.impl;

import com.demo.securities.config.DatabaseConfig;
import com.demo.securities.exception.DataAccessException;
import com.demo.securities.model.GioiTinh;
import com.demo.securities.model.KhachHang;
import com.demo.securities.repository.KhachHangRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KhachHangRepositoryImpl implements KhachHangRepository {

    private final String table = DatabaseConfig.getSchema() + ".khach_hang";

    @Override
    public List<KhachHang> findAll() {
        String sql = "SELECT * FROM " + table + " ORDER BY id";
        List<KhachHang> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Không lấy được danh sách khách hàng", e);
        }
    }

    @Override
    public Optional<KhachHang> findById(String id) {
        String sql = "SELECT * FROM " + table + " WHERE id = ?";
        return queryOne(sql, id);
    }

    @Override
    public Optional<KhachHang> findByCCCD(String soCCCD) {
        String sql = "SELECT * FROM " + table + " WHERE so_cccd = ?";
        return queryOne(sql, soCCCD);
    }

    @Override
    public List<KhachHang> searchByTen(String keyword) {
        String sql = "SELECT * FROM " + table + " WHERE LOWER(ho_ten) LIKE LOWER(?) ORDER BY id";
        List<KhachHang> result = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("Không tìm kiếm được khách hàng theo tên", e);
        }
    }

    @Override
    public void save(KhachHang khachHang) {
        String sql = "INSERT INTO " + table +
                " (id, ho_ten, ngay_sinh, gioi_tinh, so_cccd, so_dien_thoai, email, dia_chi, ngay_tao)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindForInsertOrUpdate(ps, khachHang);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Không thêm được khách hàng", e);
        }
    }

    @Override
    public void update(KhachHang khachHang) {
        String sql = "UPDATE " + table +
                " SET ho_ten = ?, ngay_sinh = ?, gioi_tinh = ?, so_cccd = ?, so_dien_thoai = ?," +
                " email = ?, dia_chi = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khachHang.getHoTen());
            ps.setDate(2, Date.valueOf(khachHang.getNgaySinh()));
            ps.setString(3, khachHang.getGioiTinh().name());
            ps.setString(4, khachHang.getSoCCCD());
            ps.setString(5, khachHang.getSoDienThoai());
            ps.setString(6, khachHang.getEmail());
            ps.setString(7, khachHang.getDiaChi());
            ps.setString(8, khachHang.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Không cập nhật được khách hàng", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String sql = "DELETE FROM " + table + " WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Không xóa được khách hàng", e);
        }
    }

    private Optional<KhachHang> queryOne(String sql, String param) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Không truy vấn được khách hàng", e);
        }
    }

    private void bindForInsertOrUpdate(PreparedStatement ps, KhachHang khachHang) throws SQLException {
        ps.setString(1, khachHang.getId());
        ps.setString(2, khachHang.getHoTen());
        ps.setDate(3, Date.valueOf(khachHang.getNgaySinh()));
        ps.setString(4, khachHang.getGioiTinh().name());
        ps.setString(5, khachHang.getSoCCCD());
        ps.setString(6, khachHang.getSoDienThoai());
        ps.setString(7, khachHang.getEmail());
        ps.setString(8, khachHang.getDiaChi());
        ps.setTimestamp(9, Timestamp.valueOf(khachHang.getNgayTao()));
    }

    private KhachHang map(ResultSet rs) throws SQLException {
        return new KhachHang(
                rs.getString("id"),
                rs.getString("ho_ten"),
                rs.getDate("ngay_sinh").toLocalDate(),
                GioiTinh.valueOf(rs.getString("gioi_tinh")),
                rs.getString("so_cccd"),
                rs.getString("so_dien_thoai"),
                rs.getString("email"),
                rs.getString("dia_chi"),
                rs.getTimestamp("ngay_tao").toLocalDateTime()
        );
    }
}
