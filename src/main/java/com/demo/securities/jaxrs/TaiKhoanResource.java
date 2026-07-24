package com.demo.securities.jaxrs;

import com.demo.securities.jaxrs.dto.OpenTaiKhoanRequest;
import com.demo.securities.jaxrs.dto.SoTienRequest;
import com.demo.securities.jaxrs.dto.TaiKhoanDto;
import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/tai-khoan")
public class TaiKhoanResource {

    @Inject
    private TaiKhoanService taiKhoanService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TaiKhoanDto> list(@QueryParam("khachHangId") String khachHangId) {
        List<TaiKhoanChungKhoan> result = (khachHangId == null || khachHangId.isBlank())
                ? taiKhoanService.danhSachTatCa()
                : taiKhoanService.danhSachTheoKhachHang(khachHangId);
        return result.stream().map(TaiKhoanResource::toDto).toList();
    }

    @GET
    @Path("/{so}")
    @Produces(MediaType.APPLICATION_JSON)
    public TaiKhoanDto getBySo(@PathParam("so") String so) {
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response open(OpenTaiKhoanRequest req) {
        double soDuBanDau = req.soDuBanDau() == null ? 0 : req.soDuBanDau();
        TaiKhoanChungKhoan taiKhoan = taiKhoanService.moTaiKhoan(
                req.khachHangId(),
                LoaiTaiKhoan.valueOf(req.loaiTaiKhoan().toUpperCase()),
                soDuBanDau);
        return Response.status(201).entity(toDto(taiKhoan)).build();
    }

    @POST
    @Path("/{so}/khoa")
    @Produces(MediaType.APPLICATION_JSON)
    public TaiKhoanDto khoa(@PathParam("so") String so) {
        taiKhoanService.khoaTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @POST
    @Path("/{so}/mo-khoa")
    @Produces(MediaType.APPLICATION_JSON)
    public TaiKhoanDto moKhoa(@PathParam("so") String so) {
        taiKhoanService.moKhoaTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @POST
    @Path("/{so}/dong")
    @Produces(MediaType.APPLICATION_JSON)
    public TaiKhoanDto dong(@PathParam("so") String so) {
        taiKhoanService.dongTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @POST
    @Path("/{so}/nap")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaiKhoanDto nap(@PathParam("so") String so, SoTienRequest req) {
        taiKhoanService.napTien(so, req.soTien());
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @POST
    @Path("/{so}/rut")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TaiKhoanDto rut(@PathParam("so") String so, SoTienRequest req) {
        taiKhoanService.rutTien(so, req.soTien());
        return toDto(taiKhoanService.timTheoSo(so));
    }

    private static TaiKhoanDto toDto(TaiKhoanChungKhoan taiKhoan) {
        return new TaiKhoanDto(
                taiKhoan.getSoTaiKhoan(),
                taiKhoan.getKhachHangId(),
                taiKhoan.getLoaiTaiKhoan().name(),
                taiKhoan.getTrangThai().name(),
                taiKhoan.getNgayMo().toString(),
                taiKhoan.getSoDuTien());
    }
}
