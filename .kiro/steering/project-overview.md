---
inclusion: auto
---

# Project Overview — Demo Java Web Service (Ôn tập gốc rễ Java & Web Service)

## Mục tiêu

Ứng dụng Java SE quản lý khách hàng mở tài khoản chứng khoán. 1 nghiệp vụ duy nhất, expose qua **14 entry point** (13 trong project chính + Quarkus tách riêng), phủ gần trọn hệ sinh thái Java web "từ xưa đến nay". Mọi entry point dùng chung service/repository/model/config, chỉ khác lớp presentation.

## Kiến trúc phân tầng

```
Presentation (14 cách khác nhau)
        ↓
Service (KhachHangService, TaiKhoanService — nghiệp vụ, validate)
        ↓
Repository (KhachHangRepositoryImpl, TaiKhoanRepositoryImpl — JDBC thuần)
        ↓
PostgreSQL (qua DatabaseConfig, đọc db.properties)
```

Ngoại lệ: `SpringFrameworkMain` đi qua Hibernate/JPA (`TaiKhoanFwService` → `TaiKhoanDao` → `EntityManager`) thay vì JDBC repo. Quarkus dùng Panache entity riêng.

## 14 Entry Point

| # | Class | Công nghệ | Port | Server |
|---|-------|-----------|------|--------|
| 1 | `Main` | Console (Scanner) | — | — |
| 2 | `WebMain` | Java SE thuần (`com.sun.net.httpserver`, tự viết Router+JSON) | 8080 | JDK built-in |
| 3 | `ServletMain` | Jakarta Servlet thuần (doGet/doPost tay) | 8083 | Embedded Tomcat 11 |
| 4 | `JaxRsMain` | JAX-RS / Jersey (annotation routing, Jackson JSON) | 8084 | Embedded Tomcat 11 |
| 5 | `SoapMain` | JAX-WS / Apache CXF SOAP (code-first) | 8085 | CXF embedded Jetty |
| 6 | `SpringMvcMain` | Spring MVC không Boot (tự dựng Tomcat+DispatcherServlet) | 8086 | Embedded Tomcat |
| 7 | `SpringBootMain` | Spring Boot (tái dùng controller MVC, 0 dòng hạ tầng) | 8087 | Auto Tomcat |
| 8 | `SpringWebFluxMain` | Spring WebFlux reactive (Mono/Flux, Netty) | 8088 | Netty |
| 9 | `SpringWsMain` | Spring-WS SOAP contract-first (XSD trước, WSDL sinh runtime) | 8089 | Embedded Tomcat |
| 10 | `SpringFrameworkMain` | Mini Spring Framework đầy đủ (IoC/MVC/TX/AOP/Security/Hibernate) | 8090 | Embedded Tomcat |
| 11 | `WebSocketMain` | Jakarta WebSocket (2 chiều thật, server push) | 8091 | Embedded Tomcat |
| 12 | `GraphQlMain` | Spring GraphQL (1 endpoint /graphql, client chọn field) | 8092 | Auto Tomcat |
| 13 | `GrpcMain` | gRPC (HTTP/2 nhị phân, protobuf, server-streaming) | 8093 | grpc-netty-shaded |
| 14 | `quarkus-app/` | Quarkus (project Maven riêng, Panache, build-time DI) | 8094 | Vert.x |

## Cấu trúc package chính

```
src/main/java/com/demo/securities/
  model/          Domain: KhachHang, TaiKhoanChungKhoan, enum GioiTinh/LoaiTaiKhoan/TrangThaiTaiKhoan
  exception/      ValidationException, NotFoundException, DuplicateException, DataAccessException
  util/           Validator, IdGenerator
  config/         DatabaseConfig (JDBC connection factory)
  repository/     Interface + impl JDBC
  service/        Business logic dùng chung (trừ SpringFrameworkMain có service Hibernate riêng)
  ui/             ConsoleMenu
  tool/           SchemaInitializer
  web/            Custom HTTP framework cho WebMain: Router, WebServer, JsonWriter/JsonParser, handler/
  servlet/        Raw Servlet (BaseApiServlet, KhachHangServlet, TaiKhoanServlet)
  jaxrs/          JAX-RS/Jersey (Resource, DTO, ExceptionMapper)
  soap/           JAX-WS/CXF (SEI, Impl, SoapDto, FaultException)
  spring/         Spring MVC shared (AppConfig, Controller, GlobalExceptionHandler, DTO)
  springmvc/      WebMvcConfig (chỉ SpringMvcMain dùng)
  springboot/     OptimisticLockDemoController (chỉ SpringBootMain dùng)
  springflux/     Reactive controller (Mono/Flux wrapper)
  springws/       Spring-WS (@Endpoint, JAXB, WsConfig)
  springfw/       Full Spring Framework: entity/, dao/, service/, aop/, config/, web/
  grpc/           gRPC service impl (code gen từ .proto)
  websocket/      @ServerEndpoint
  graphql/        @Controller + @QueryMapping/@MutationMapping
```

## Contract-first resources

- `src/main/proto/tai_khoan.proto` — gRPC contract (7 unary + 1 server-streaming)
- `src/main/resources/tai-khoan-ws.xsd` — Spring-WS XSD
- `src/main/resources/graphql/schema.graphqls` — GraphQL SDL

## Database

PostgreSQL, 2 bảng trong schema `account_management`:
- `khach_hang` (id PK, ho_ten, ngay_sinh, gioi_tinh, so_cccd UNIQUE, so_dien_thoai, email, dia_chi, ngay_tao)
- `tai_khoan_chung_khoan` (so_tai_khoan PK, khach_hang_id FK, loai_tai_khoan, trang_thai, ngay_mo, so_du_tien, version)

Cột `version` dùng cho Optimistic Lock (Hibernate/JPA ở SpringFrameworkMain và SpringBootMain).

## Build & Run

- JDK 21+, Maven
- `db.properties` — cấu hình JDBC (copy từ `db.properties.example`)
- Tạo bảng: `mvn compile exec:java -Dexec.mainClass=com.demo.securities.tool.SchemaInitializer`
- Chạy entry point: `mvn compile exec:java "-Dexec.mainClass=com.demo.securities.<TenMain>" "-Dserver.port=<port>"`
- Quarkus: `cd quarkus-app && mvn quarkus:dev`
- Đóng gói: `mvn package` → uber-jar via shade plugin

## Dependencies chính (pom.xml)

PostgreSQL JDBC 42.7, Tomcat Embed 11.0, Jersey 3.1, Apache CXF 4.2, Spring Boot 4.1, Spring Framework 7, Spring Security 7.1, Spring-WS 5.0, Hibernate 7.4, gRPC 1.83, Protobuf 3.25, Jackson, AspectJ, `tomcat-embed-websocket`, `spring-boot-starter-graphql`, `spring-boot-starter-data-jpa`.

Build plugins: `protobuf-maven-plugin` (sinh gRPC stub), `maven-shade-plugin` (uber-jar), `exec-maven-plugin`, `os-maven-plugin` (detect OS cho protoc).
