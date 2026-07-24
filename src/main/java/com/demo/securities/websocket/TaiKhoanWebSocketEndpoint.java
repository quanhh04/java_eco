package com.demo.securities.websocket;

import com.demo.securities.exception.NotFoundException;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.web.json.JsonWriter;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minh hoa dung ban chat 2 chieu (bidirectional) cua WebSocket: client ket noi,
 * server day ngay trang thai hien tai; client gui message "refresh" thi server
 * truy van lai DB va day state moi - khac han REST (client luon phai tu hoi lai).
 *
 * Container tu tao 1 instance endpoint MOI cho MOI ket noi (khong qua DI nao ca),
 * nen dung field static de tham chieu TaiKhoanService - gan 1 lan luc WebSocketMain
 * khoi dong. Don gian nhat cho quy mo demo nay.
 */
@ServerEndpoint("/ws/tai-khoan/{soTaiKhoan}")
public class TaiKhoanWebSocketEndpoint {

    public static TaiKhoanService taiKhoanService;

    @OnOpen
    public void onOpen(Session session, @PathParam("soTaiKhoan") String soTaiKhoan) throws IOException {
        pushState(session, soTaiKhoan);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("soTaiKhoan") String soTaiKhoan) throws IOException {
        if ("refresh".equalsIgnoreCase(message.trim())) {
            pushState(session, soTaiKhoan);
        } else {
            session.getBasicRemote().sendText(JsonWriter.write(Map.of("error", "Lenh khong ho tro: " + message)));
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("[WebSocket] Dong ket noi: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("[WebSocket] Loi tren ket noi " + session.getId() + ": " + error.getMessage());
    }

    private void pushState(Session session, String soTaiKhoan) throws IOException {
        try {
            TaiKhoanChungKhoan taiKhoan = taiKhoanService.timTheoSo(soTaiKhoan);
            session.getBasicRemote().sendText(JsonWriter.write(toMap(taiKhoan)));
        } catch (NotFoundException e) {
            session.getBasicRemote().sendText(JsonWriter.write(Map.of("error", e.getMessage())));
        }
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
