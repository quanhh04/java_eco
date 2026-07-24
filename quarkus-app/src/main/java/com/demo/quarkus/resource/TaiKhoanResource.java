package com.demo.quarkus.resource;

import com.demo.quarkus.dto.OpenTaiKhoanRequest;
import com.demo.quarkus.dto.SoTienRequest;
import com.demo.quarkus.dto.TaiKhoanDto;
import com.demo.quarkus.entity.TaiKhoan;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

/**
 * Nhanh "Framework khac khong Spring - Quarkus": @Path/@GET/@POST y het chuan
 * JAX-RS da dung o JaxRsMain (quarkus-rest tuong thich API), nhung DI la
 * build-time (Quarkus "augmentation" sinh bytecode luc compile, khong dung
 * reflection runtime nhu Jersey/HK2) va Panache thay the han DAO/EntityManager
 * tay. Day la project Maven RIENG (classpath tach biet 13 entry point kia) nen
 * validate tuoi khach hang bang 1 cau JDBC truc tiep, khong tai dung duoc
 * KhachHangService cua project chinh.
 */
@Path("/api/tai-khoan")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TaiKhoanResource {

    @Inject
    DataSource dataSource;

    @GET
    public List<TaiKhoanDto> list(@QueryParam("khachHangId") String khachHangId) {
        List<TaiKhoan> result = (khachHangId == null || khachHangId.isBlank())
                ? TaiKhoan.listAll()
                : TaiKhoan.danhSachTheoKhachHang(khachHangId);
        return result.stream().map(TaiKhoanResource::toDto).toList();
    }

    @GET
    @Path("/{so}")
    public TaiKhoanDto getBySo(@PathParam("so") String so) {
        return toDto(timHoacNem(so));
    }

    @POST
    @Transactional
    public Response open(OpenTaiKhoanRequest req) {
        LocalDate ngaySinh = timNgaySinh(req.khachHangId())
                .orElseThrow(() -> notFound("Khong tim thay khach hang: " + req.khachHangId()));
        if (Period.between(ngaySinh, LocalDate.now()).getYears() < 18) {
            throw badRequest("Khach hang phai du 18 tuoi de mo tai khoan chung khoan");
        }
        double soDuBanDau = req.soDuBanDau() == null ? 0 : req.soDuBanDau();
        if (soDuBanDau < 0) {
            throw badRequest("So du ban dau khong duoc am");
        }

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.soTaiKhoan = sinhSoTaiKhoanMoi();
        taiKhoan.khachHangId = req.khachHangId();
        taiKhoan.loaiTaiKhoan = TaiKhoan.LoaiTaiKhoan.valueOf(req.loaiTaiKhoan().toUpperCase());
        taiKhoan.trangThai = TaiKhoan.TrangThaiTaiKhoan.HOAT_DONG;
        taiKhoan.ngayMo = LocalDateTime.now();
        taiKhoan.soDuTien = BigDecimal.valueOf(soDuBanDau);
        taiKhoan.persist();
        return Response.status(Response.Status.CREATED).entity(toDto(taiKhoan)).build();
    }

    @POST
    @Path("/{so}/khoa")
    @Transactional
    public TaiKhoanDto khoa(@PathParam("so") String so) {
        TaiKhoan taiKhoan = timHoacNem(so);
        if (taiKhoan.trangThai != TaiKhoan.TrangThaiTaiKhoan.HOAT_DONG) {
            throw badRequest("Chi co the khoa tai khoan dang hoat dong");
        }
        taiKhoan.trangThai = TaiKhoan.TrangThaiTaiKhoan.TAM_KHOA;
        return toDto(taiKhoan);
    }

    @POST
    @Path("/{so}/mo-khoa")
    @Transactional
    public TaiKhoanDto moKhoa(@PathParam("so") String so) {
        TaiKhoan taiKhoan = timHoacNem(so);
        if (taiKhoan.trangThai != TaiKhoan.TrangThaiTaiKhoan.TAM_KHOA) {
            throw badRequest("Chi co the mo khoa tai khoan dang tam khoa");
        }
        taiKhoan.trangThai = TaiKhoan.TrangThaiTaiKhoan.HOAT_DONG;
        return toDto(taiKhoan);
    }

    @POST
    @Path("/{so}/dong")
    @Transactional
    public TaiKhoanDto dong(@PathParam("so") String so) {
        TaiKhoan taiKhoan = timHoacNem(so);
        if (taiKhoan.trangThai == TaiKhoan.TrangThaiTaiKhoan.DA_DONG) {
            throw badRequest("Tai khoan da duoc dong truoc do");
        }
        if (taiKhoan.soDuTien.compareTo(BigDecimal.ZERO) != 0) {
            throw badRequest("So du phai bang 0 truoc khi dong tai khoan");
        }
        taiKhoan.trangThai = TaiKhoan.TrangThaiTaiKhoan.DA_DONG;
        return toDto(taiKhoan);
    }

    @POST
    @Path("/{so}/nap")
    @Transactional
    public TaiKhoanDto nap(@PathParam("so") String so, SoTienRequest req) {
        TaiKhoan taiKhoan = timHoacNem(so);
        kiemTraHoatDong(taiKhoan);
        if (req.soTien() <= 0) {
            throw badRequest("So tien nap phai lon hon 0");
        }
        taiKhoan.soDuTien = taiKhoan.soDuTien.add(BigDecimal.valueOf(req.soTien()));
        return toDto(taiKhoan);
    }

    @POST
    @Path("/{so}/rut")
    @Transactional
    public TaiKhoanDto rut(@PathParam("so") String so, SoTienRequest req) {
        TaiKhoan taiKhoan = timHoacNem(so);
        kiemTraHoatDong(taiKhoan);
        if (req.soTien() <= 0) {
            throw badRequest("So tien rut phai lon hon 0");
        }
        if (BigDecimal.valueOf(req.soTien()).compareTo(taiKhoan.soDuTien) > 0) {
            throw badRequest("So du khong du de rut");
        }
        taiKhoan.soDuTien = taiKhoan.soDuTien.subtract(BigDecimal.valueOf(req.soTien()));
        return toDto(taiKhoan);
    }

    private void kiemTraHoatDong(TaiKhoan taiKhoan) {
        if (taiKhoan.trangThai != TaiKhoan.TrangThaiTaiKhoan.HOAT_DONG) {
            throw badRequest("Tai khoan khong o trang thai hoat dong");
        }
    }

    private TaiKhoan timHoacNem(String so) {
        TaiKhoan taiKhoan = TaiKhoan.timTheoSo(so);
        if (taiKhoan == null) {
            throw notFound("Khong tim thay tai khoan: " + so);
        }
        return taiKhoan;
    }

    private String sinhSoTaiKhoanMoi() {
        int max = 0;
        for (TaiKhoan tk : TaiKhoan.<TaiKhoan>listAll()) {
            try {
                max = Math.max(max, Integer.parseInt(tk.soTaiKhoan.substring(2)));
            } catch (NumberFormatException ignored) {
                // so tai khoan khong dung dinh dang so, bo qua khi tinh max
            }
        }
        return "TK" + String.format("%06d", max + 1);
    }

    private Optional<LocalDate> timNgaySinh(String khachHangId) {
        String sql = "SELECT ngay_sinh FROM account_management.khach_hang WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khachHangId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(rs.getDate("ngay_sinh").toLocalDate());
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Loi truy van khach_hang: " + e.getMessage(), e);
        }
    }

    private static WebApplicationException notFound(String message) {
        return new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .entity(java.util.Map.of("error", message)).build());
    }

    private static WebApplicationException badRequest(String message) {
        return new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                .entity(java.util.Map.of("error", message)).build());
    }

    private static TaiKhoanDto toDto(TaiKhoan taiKhoan) {
        return new TaiKhoanDto(
                taiKhoan.soTaiKhoan,
                taiKhoan.khachHangId,
                taiKhoan.loaiTaiKhoan.name(),
                taiKhoan.trangThai.name(),
                taiKhoan.ngayMo.toString(),
                taiKhoan.soDuTien.doubleValue());
    }
}
