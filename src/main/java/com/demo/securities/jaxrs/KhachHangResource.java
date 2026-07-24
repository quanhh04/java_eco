package com.demo.securities.jaxrs;

import com.demo.securities.jaxrs.dto.CreateKhachHangRequest;
import com.demo.securities.jaxrs.dto.KhachHangDto;
import com.demo.securities.model.GioiTinh;
import com.demo.securities.model.KhachHang;
import com.demo.securities.service.KhachHangService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.List;

@Path("/khach-hang")
public class KhachHangResource {

    @Inject
    private KhachHangService khachHangService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<KhachHangDto> list(@QueryParam("ten") String ten) {
        List<KhachHang> result = (ten == null || ten.isBlank())
                ? khachHangService.danhSach()
                : khachHangService.timTheoTen(ten);
        return result.stream().map(KhachHangResource::toDto).toList();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public KhachHangDto getById(@PathParam("id") String id) {
        return toDto(khachHangService.timTheoId(id));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(CreateKhachHangRequest req) {
        KhachHang khachHang = khachHangService.themKhachHang(
                req.hoTen(),
                LocalDate.parse(req.ngaySinh()),
                GioiTinh.valueOf(req.gioiTinh().toUpperCase()),
                req.soCCCD(),
                req.soDienThoai(),
                req.email(),
                req.diaChi());
        return Response.status(201).entity(toDto(khachHang)).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public KhachHangDto update(@PathParam("id") String id, CreateKhachHangRequest req) {
        KhachHang khachHang = khachHangService.suaKhachHang(
                id,
                req.hoTen(),
                LocalDate.parse(req.ngaySinh()),
                GioiTinh.valueOf(req.gioiTinh().toUpperCase()),
                req.soDienThoai(),
                req.email(),
                req.diaChi());
        return toDto(khachHang);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        khachHangService.xoaKhachHang(id);
        return Response.noContent().build();
    }

    private static KhachHangDto toDto(KhachHang khachHang) {
        return new KhachHangDto(
                khachHang.getId(),
                khachHang.getHoTen(),
                khachHang.getNgaySinh().toString(),
                khachHang.getGioiTinh().name(),
                khachHang.getSoCCCD(),
                khachHang.getSoDienThoai(),
                khachHang.getEmail(),
                khachHang.getDiaChi(),
                khachHang.getNgayTao().toString());
    }
}
