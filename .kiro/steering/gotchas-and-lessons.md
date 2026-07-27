---
inclusion: auto
---

# Gotcha & Bài học thực chiến

## Lỗi Classloader Tomcat nhúng (gặp 3 lần)

**Triệu chứng**: `ClassNotFoundException` khi Spring/CXF/Hibernate tra cứu class qua reflection (default strategies, JPA provider, ServiceLoader...) bên trong Tomcat embedded context.

**Nguyên nhân**: `tomcat.addContext(...)` tạo `WebappClassLoader` KHÔNG tự kế thừa đúng classloader ứng dụng (nơi chứa tất cả dependency đã load).

**Fix**: `ctx.setParentClassLoader(XxxMain.class.getClassLoader())` ngay sau `addContext()`.

**Áp dụng cho**: SpringWsMain, SpringFrameworkMain, WebSocketMain.

## Filter khởi tạo trước Servlet (SpringFrameworkMain)

**Triệu chứng**: `"No ServletContext set"` lỗi từ `@EnableWebMvc` (resourceHandlerMapping).

**Nguyên nhân**: Tomcat init Filter TRƯỚC Servlet. `DelegatingFilterProxy` (Security) là filter đầu tiên chạm tới context → tự trigger `refresh()` khi chưa có `ServletContext` gắn vào context.

**Fix**: Tự `context.setServletContext(ctx.getServletContext())` + `context.refresh()` TRƯỚC khi đăng ký cả filter lẫn servlet. Sau đó cả 2 thấy context đã active, không refresh lần nữa.

## WebSocket cần ít nhất 1 servlet (WebSocketMain)

**Triệu chứng**: Mọi WebSocket handshake bị 404 (kể cả request hợp lệ).

**Nguyên nhân**: Tomcat's request Mapper cần ít nhất 1 servlet match path thì filter chain (bao gồm `WsFilter` — thứ chặn HTTP Upgrade) mới được gọi tới. Context không có servlet → Mapper trả 404 trước khi chạm `WsFilter`.

**Fix**: Thêm 1 servlet catch-all `/*` (chỉ trả 404 cho HTTP thường):
```java
Tomcat.addServlet(ctx, "default", new HttpServlet() { ... });
ctx.addServletMappingDecoded("/*", "default");
```

**Đây là lỗi tinh vi nhất** — không phải lỗi classloader, không có error log rõ ràng.

## Xung đột version jackson-annotations

**Triệu chứng**: `NoClassDefFoundError` khi chạy Spring Boot entry point.

**Nguyên nhân**: Jersey kéo `jackson-annotations:2.19.1`, Spring Boot 4 cần `2.21` (class mới hơn). Maven mediation ưu tiên nhầm bản cũ.

**Fix**: Khai tường minh `jackson-annotations:2.21` làm dependency trực tiếp trong pom.xml.

## context.scan() không hoạt động (SpringMvcMain)

**Triệu chứng**: Controller không được khởi tạo, không lỗi, không log.

**Nguyên nhân**: `context.scan("com.demo.securities.spring")` trên Spring Framework 7 trong setup thủ công không quét đúng.

**Fix**: Chuyển sang `context.register(Controller.class, ...)` liệt kê tường minh từng class.

## Protobuf version nhầm

**Triệu chứng**: Incompatible class khi chạy gRPC.

**Nguyên nhân**: Maven Central có `protobuf-java:4.x` (mới nhất), nhưng `grpc-protobuf:1.83.0` cần đúng `protobuf-java:3.25.9`.

**Fix**: Kiểm tra POM của `grpc-protobuf` trực tiếp trước khi ghim version. Dùng `grpc-netty-shaded` (không phải `grpc-netty`) để tránh xung đột Netty với WebFlux.

## Spring-WS ClassNotFoundException (SpringWsMain)

**Triệu chứng**: `ClassNotFoundException` cho `SaajSoapMessageFactory`, `SoapMessageDispatcher`.

**Nguyên nhân**: Spring-WS tra cứu "default strategy" qua `ClassUtils.forName` reflection — nhạy cảm với thread context classloader trong Tomcat nhúng.

**Fix**: 
1. `ctx.setParentClassLoader(SpringWsMain.class.getClassLoader())`
2. Khai tường minh bean `WebServiceMessageFactory` trong WsConfig
3. `setLoadOnStartup(1)` để init trên main thread (không phải worker thread)

## Auto-configuration GraphQL bật toàn cục

**Triệu chứng**: Log "GraphQL schema inspection" xuất hiện khi chạy SpringBootMain (không liên quan GraphQL).

**Nguyên nhân**: `spring-boot-starter-graphql` nằm chung pom.xml + `schema.graphqls` trên classpath chung → auto-config bật bất kể `scanBasePackages` giới hạn gì.

**Fix**: Vô hại (chỉ warning), nhưng cần biết khi nhiều entry point Boot dùng chung pom.xml. Nếu cần tắt: exclude auto-config class tường minh.

## Quarkus groupId nhầm

**Vấn đề**: Nhầm `io.quarkus.platform:quarkus-bom` vs `io.quarkus:quarkus-bom`.

**Giải thích**: `io.quarkus.platform` là BOM tổng hợp (bao gồm extension bên thứ 3), `io.quarkus` là BOM lõi (chỉ extension chính thức). Cả 2 đều tồn tại trên Maven Central.

## Quarkus jar name thay đổi

**Cũ (1.x/2.x)**: `target/<artifact>-runner.jar`  
**Mới (3.x)**: `target/quarkus-app/quarkus-run.jar` (thin jar + thư mục lib/app/quarkus/)

## IdGenerator race condition

`IdGenerator` dùng pattern `max(existing) + 1` bằng full-scan list. An toàn cho console (đơn luồng), nhưng có race condition nếu nhiều request tạo mới đồng thời qua web service. Đây là giới hạn đã biết, không fix (giữ đơn giản cho demo).

## Cột version (Optimistic Lock) chỉ được Hibernate quản lý

Các entry point JDBC thuần (`TaiKhoanRepositoryImpl`) KHÔNG đọc/ghi cột `version` → optimistic lock chỉ bảo vệ xung đột giữa writer đi qua JPA, không bảo vệ nếu writer JDBC xen vào.
