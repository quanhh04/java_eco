package com.demo.securities.web.handler;

import com.demo.securities.model.GioiTinh;
import com.demo.securities.model.KhachHang;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.web.HttpUtil;
import com.demo.securities.web.RequestBody;
import com.demo.securities.web.Router;
import com.sun.net.httpserver.HttpExchange;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KhachHangHandler {

    private final KhachHangService khachHangService;

    public KhachHangHandler(KhachHangService khachHangService) {
        this.khachHangService = khachHangService;
    }

    public void register(Router router) {
        router.get("/api/khach-hang", this::list);
        router.get("/api/khach-hang/{id}", this::getById);
        router.post("/api/khach-hang", this::create);
        router.put("/api/khach-hang/{id}", this::update);
        router.delete("/api/khach-hang/{id}", this::delete);
    }

    private void list(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        String ten = query.get("ten");
        List<KhachHang> result = (ten == null || ten.isBlank())
                ? khachHangService.danhSach()
                : khachHangService.timTheoTen(ten);
        HttpUtil.sendJson(exchange, 200, result.stream().map(KhachHangHandler::toMap).toList());
    }

    private void getById(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        KhachHang khachHang = khachHangService.timTheoId(pathParams.get("id"));
        HttpUtil.sendJson(exchange, 200, toMap(khachHang));
    }

    private void create(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        Map<String, Object> body = RequestBody.parseObject(HttpUtil.readBody(exchange));
        KhachHang khachHang = khachHangService.themKhachHang(
                RequestBody.requireString(body, "hoTen"),
                LocalDate.parse(RequestBody.requireString(body, "ngaySinh")),
                GioiTinh.valueOf(RequestBody.requireString(body, "gioiTinh").toUpperCase()),
                RequestBody.requireString(body, "soCCCD"),
                RequestBody.requireString(body, "soDienThoai"),
                RequestBody.requireString(body, "email"),
                RequestBody.requireString(body, "diaChi"));
        HttpUtil.sendJson(exchange, 201, toMap(khachHang));
    }

    private void update(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        Map<String, Object> body = RequestBody.parseObject(HttpUtil.readBody(exchange));
        KhachHang khachHang = khachHangService.suaKhachHang(
                pathParams.get("id"),
                RequestBody.requireString(body, "hoTen"),
                LocalDate.parse(RequestBody.requireString(body, "ngaySinh")),
                GioiTinh.valueOf(RequestBody.requireString(body, "gioiTinh").toUpperCase()),
                RequestBody.requireString(body, "soDienThoai"),
                RequestBody.requireString(body, "email"),
                RequestBody.requireString(body, "diaChi"));
        HttpUtil.sendJson(exchange, 200, toMap(khachHang));
    }

    private void delete(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        khachHangService.xoaKhachHang(pathParams.get("id"));
        HttpUtil.sendNoContent(exchange, 204);
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
