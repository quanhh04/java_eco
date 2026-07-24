package com.demo.securities.web.handler;

import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.web.HttpUtil;
import com.demo.securities.web.RequestBody;
import com.demo.securities.web.Router;
import com.sun.net.httpserver.HttpExchange;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TaiKhoanHandler {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanHandler(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    public void register(Router router) {
        router.get("/api/tai-khoan", this::list);
        router.get("/api/tai-khoan/{so}", this::getBySo);
        router.post("/api/tai-khoan", this::open);
        router.post("/api/tai-khoan/{so}/khoa", this::khoa);
        router.post("/api/tai-khoan/{so}/mo-khoa", this::moKhoa);
        router.post("/api/tai-khoan/{so}/dong", this::dong);
        router.post("/api/tai-khoan/{so}/nap", this::nap);
        router.post("/api/tai-khoan/{so}/rut", this::rut);
    }

    private void list(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        Map<String, String> query = HttpUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        String khachHangId = query.get("khachHangId");
        List<TaiKhoanChungKhoan> result = (khachHangId == null || khachHangId.isBlank())
                ? taiKhoanService.danhSachTatCa()
                : taiKhoanService.danhSachTheoKhachHang(khachHangId);
        HttpUtil.sendJson(exchange, 200, result.stream().map(TaiKhoanHandler::toMap).toList());
    }

    private void getBySo(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        TaiKhoanChungKhoan taiKhoan = taiKhoanService.timTheoSo(pathParams.get("so"));
        HttpUtil.sendJson(exchange, 200, toMap(taiKhoan));
    }

    private void open(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        Map<String, Object> body = RequestBody.parseObject(HttpUtil.readBody(exchange));
        TaiKhoanChungKhoan taiKhoan = taiKhoanService.moTaiKhoan(
                RequestBody.requireString(body, "khachHangId"),
                LoaiTaiKhoan.valueOf(RequestBody.requireString(body, "loaiTaiKhoan").toUpperCase()),
                RequestBody.optionalDouble(body, "soDuBanDau", 0));
        HttpUtil.sendJson(exchange, 201, toMap(taiKhoan));
    }

    private void khoa(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        taiKhoanService.khoaTaiKhoan(pathParams.get("so"));
        HttpUtil.sendJson(exchange, 200, toMap(taiKhoanService.timTheoSo(pathParams.get("so"))));
    }

    private void moKhoa(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        taiKhoanService.moKhoaTaiKhoan(pathParams.get("so"));
        HttpUtil.sendJson(exchange, 200, toMap(taiKhoanService.timTheoSo(pathParams.get("so"))));
    }

    private void dong(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        taiKhoanService.dongTaiKhoan(pathParams.get("so"));
        HttpUtil.sendJson(exchange, 200, toMap(taiKhoanService.timTheoSo(pathParams.get("so"))));
    }

    private void nap(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        Map<String, Object> body = RequestBody.parseObject(HttpUtil.readBody(exchange));
        taiKhoanService.napTien(pathParams.get("so"), RequestBody.requireDouble(body, "soTien"));
        HttpUtil.sendJson(exchange, 200, toMap(taiKhoanService.timTheoSo(pathParams.get("so"))));
    }

    private void rut(HttpExchange exchange, Map<String, String> pathParams) throws Exception {
        Map<String, Object> body = RequestBody.parseObject(HttpUtil.readBody(exchange));
        taiKhoanService.rutTien(pathParams.get("so"), RequestBody.requireDouble(body, "soTien"));
        HttpUtil.sendJson(exchange, 200, toMap(taiKhoanService.timTheoSo(pathParams.get("so"))));
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
