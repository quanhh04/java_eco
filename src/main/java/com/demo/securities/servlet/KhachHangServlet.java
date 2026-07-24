package com.demo.securities.servlet;

import com.demo.securities.model.GioiTinh;
import com.demo.securities.model.KhachHang;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.web.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KhachHangServlet extends BaseApiServlet {

    private final KhachHangService khachHangService;

    public KhachHangServlet(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                String ten = req.getParameter("ten");
                List<KhachHang> result = (ten == null || ten.isBlank())
                        ? khachHangService.danhSach()
                        : khachHangService.timTheoTen(ten);
                ServletHttpUtil.sendJson(resp, 200, result.stream().map(KhachHangServlet::toMap).toList());
            } else {
                String id = pathInfo.substring(1);
                ServletHttpUtil.sendJson(resp, 200, toMap(khachHangService.timTheoId(id)));
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            Map<String, Object> body = RequestBody.parseObject(ServletHttpUtil.readBody(req));
            KhachHang khachHang = khachHangService.themKhachHang(
                    RequestBody.requireString(body, "hoTen"),
                    LocalDate.parse(RequestBody.requireString(body, "ngaySinh")),
                    GioiTinh.valueOf(RequestBody.requireString(body, "gioiTinh").toUpperCase()),
                    RequestBody.requireString(body, "soCCCD"),
                    RequestBody.requireString(body, "soDienThoai"),
                    RequestBody.requireString(body, "email"),
                    RequestBody.requireString(body, "diaChi"));
            ServletHttpUtil.sendJson(resp, 201, toMap(khachHang));
        });
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            String id = req.getPathInfo().substring(1);
            Map<String, Object> body = RequestBody.parseObject(ServletHttpUtil.readBody(req));
            KhachHang khachHang = khachHangService.suaKhachHang(
                    id,
                    RequestBody.requireString(body, "hoTen"),
                    LocalDate.parse(RequestBody.requireString(body, "ngaySinh")),
                    GioiTinh.valueOf(RequestBody.requireString(body, "gioiTinh").toUpperCase()),
                    RequestBody.requireString(body, "soDienThoai"),
                    RequestBody.requireString(body, "email"),
                    RequestBody.requireString(body, "diaChi"));
            ServletHttpUtil.sendJson(resp, 200, toMap(khachHang));
        });
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            String id = req.getPathInfo().substring(1);
            khachHangService.xoaKhachHang(id);
            ServletHttpUtil.sendNoContent(resp, 204);
        });
    }

    private static Map<String, Object> toMap(KhachHang khachHang) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", khachHang.getId());
        map.put("hoTen", khachHang.getHoTen());
        map.put("ngaySinh", khachHang.getNgaySinh().toString());
        map.put("gioiTinh", khachHang.getGioiTinh().name());
        map.put("soCCCD", khachHang.getSoCCCD());
        map.put("soDienThoai", khachHang.getSoDienThoai());
        map.put("email", khachHang.getEmail());
        map.put("diaChi", khachHang.getDiaChi());
        map.put("ngayTao", khachHang.getNgayTao().toString());
        return map;
    }
}
