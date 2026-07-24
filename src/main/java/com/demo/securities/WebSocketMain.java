package com.demo.securities;

import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.repository.impl.KhachHangRepositoryImpl;
import com.demo.securities.repository.impl.TaiKhoanRepositoryImpl;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.websocket.TaiKhoanWebSocketEndpoint;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.ServerContainer;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.websocket.server.WsSci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Nhanh "Giao thuc khac - WebSocket": ket noi 2 chieu (bidirectional) that su,
 * khac han REST/gRPC unary/SOAP - client va server co the tu gui message bat ky
 * luc nao tren cung 1 ket noi TCP da mo.
 */
public class WebSocketMain {

    public static void main(String[] args) throws Exception {
        KhachHangRepository khachHangRepository = new KhachHangRepositoryImpl();
        TaiKhoanRepository taiKhoanRepository = new TaiKhoanRepositoryImpl();
        KhachHangService khachHangService = new KhachHangService(khachHangRepository, taiKhoanRepository);
        TaiKhoanService taiKhoanService = new TaiKhoanService(taiKhoanRepository, khachHangService);
        TaiKhoanWebSocketEndpoint.taiKhoanService = taiKhoanService;

        int port = Integer.getInteger("server.port", 8091);

        Path baseDir = Files.createTempDirectory("websocket-tomcat");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", baseDir.toAbsolutePath().toString());
        ctx.setParentClassLoader(WebSocketMain.class.getClassLoader());

        // Gotcha phat hien khi build: Tomcat can it nhat 1 servlet khop path thi Mapper
        // moi cho request di qua duoc filter chain (bao gom WsFilter, thu phan chan yeu
        // cau HTTP Upgrade). Context nay khong dinh nghia servlet nao khac nen phai them
        // 1 servlet catch-all "/*" chi de Mapper tim thay match - thieu no thi moi request
        // (ke ca WebSocket handshake hop le) deu bi tra ve 404 truoc khi cham toi WsFilter.
        Tomcat.addServlet(ctx, "default", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.sendError(404, "Khong co endpoint HTTP thuong o day, chi co WebSocket");
            }
        });
        ctx.addServletMappingDecoded("/*", "default");

        // WsSci: ServletContainerInitializer day du cua Tomcat cho WebSocket - dung tao
        // ServerContainer va dang ky WsFilter (chan yeu cau HTTP Upgrade) vao context.
        ctx.addServletContainerInitializer(new WsSci(), Set.of(TaiKhoanWebSocketEndpoint.class));

        tomcat.start();

        // Dang ky endpoint tuong minh - annotation @ServerEndpoint khong tu duoc quet vi
        // context dung addContext khong bat class-scanning toan bo classpath.
        ServerContainer serverContainer = (ServerContainer) ctx.getServletContext()
                .getAttribute(ServerContainer.class.getName());
        serverContainer.addEndpoint(TaiKhoanWebSocketEndpoint.class);

        System.out.println("WebSocket server dang chay tai ws://localhost:" + port + "/ws/tai-khoan/{soTaiKhoan}");
        tomcat.getServer().await();
    }
}
