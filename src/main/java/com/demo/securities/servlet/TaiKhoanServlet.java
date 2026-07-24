package com.demo.securities.servlet;

import com.demo.securities.exception.NotFoundException;
import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.web.RequestBody;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaiKhoanServlet extends BaseApiServlet {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanServlet(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                String khachHangId = req.getParameter("khachHangId");
                List<TaiKhoanChungKhoan> result = (khachHangId == null || khachHangId.isBlank())
                        ? taiKhoanService.danhSachTatCa()
                        : taiKhoanService.danhSachTheoKhachHang(khachHangId);
                ServletHttpUtil.sendJson(resp, 200, result.stream().map(TaiKhoanServlet::toMap).toList());
            } else {
                String so = pathInfo.substring(1);
                ServletHttpUtil.sendJson(resp, 200, toMap(taiKhoanService.timTheoSo(so)));
            }
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(resp, () -> {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                Map<String, Object> body = RequestBody.parseObject(ServletHttpUtil.readBody(req));
                TaiKhoanChungKhoan taiKhoan = taiKhoanService.moTaiKhoan(
                        RequestBody.requireString(body, "khachHangId"),
                        LoaiTaiKhoan.valueOf(RequestBody.requireString(body, "loaiTaiKhoan").toUpperCase()),
                        RequestBody.optionalDouble(body, "soDuBanDau", 0));
                ServletHttpUtil.sendJson(resp, 201, toMap(taiKhoan));
                return;
            }

            String[] segments = pathInfo.substring(1).split("/");
            String so = segments[0];
            String action = segments.length > 1 ? segments[1] : null;
            if (action == null) {
                throw new NotFoundException("Khong tim thay hanh dong cho tai khoan: " + so);
            }
            switch (action) {
                case "khoa" -> taiKhoanService.khoaTaiKhoan(so);
                case "mo-khoa" -> taiKhoanService.moKhoaTaiKhoan(so);
                case "dong" -> taiKhoanService.dongTaiKhoan(so);
                case "nap" -> {
                    Map<String, Object> body = RequestBody.parseObject(ServletHttpUtil.readBody(req));
                    taiKhoanService.napTien(so, RequestBody.requireDouble(body, "soTien"));
                }
                case "rut" -> {
                    Map<String, Object> body = RequestBody.parseObject(ServletHttpUtil.readBody(req));
                    taiKhoanService.rutTien(so, RequestBody.requireDouble(body, "soTien"));
                }
                default -> throw new NotFoundException("Hanh dong khong hop le: " + action);
            }
            ServletHttpUtil.sendJson(resp, 200, toMap(taiKhoanService.timTheoSo(so)));
        });
    }

    private static Map<String, Object> toMap(TaiKhoanChungKhoan taiKhoan) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("soTaiKhoan", taiKhoan.getSoTaiKhoan());
        map.put("khachHangId", taiKhoan.getKhachHangId());
        map.put("loaiTaiKhoan", taiKhoan.getLoaiTaiKhoan().name());
        map.put("trangThai", taiKhoan.getTrangThai().name());
        map.put("ngayMo", taiKhoan.getNgayMo().toString());
        map.put("soDuTien", taiKhoan.getSoDuTien());
        return map;
    }
}
