---
inclusion: auto
---

# Web Service Evolution — Từ thô đến framework

## Lộ trình tiến hóa trong project

```
Java SE thuần (WebMain, port 8080)
  → Jakarta Servlet (ServletMain, 8083)
    → JAX-RS/Jersey (JaxRsMain, 8084)
      → JAX-WS/SOAP (SoapMain, 8085)
        → Spring MVC (SpringMvcMain, 8086)
          → Spring Boot (SpringBootMain, 8087)
            → Spring WebFlux (SpringWebFluxMain, 8088)
              → Spring-WS SOAP (SpringWsMain, 8089)
                → Spring Framework đầy đủ (SpringFrameworkMain, 8090)
                  → WebSocket (WebSocketMain, 8091)
                    → GraphQL (GraphQlMain, 8092)
                      → gRPC (GrpcMain, 8093)
                        → Quarkus (quarkus-app, 8094)
```

## So sánh mức độ "tự viết" vs "framework lo"

| Entry Point | Routing | JSON | Error handling | Server setup |
|-------------|---------|------|----------------|--------------|
| WebMain | Tự viết Router | Tự viết JsonWriter/Parser | Tự catch + map HTTP status | Tự dựng HttpServer |
| ServletMain | if/else pathInfo | Tái dùng JsonWriter/Parser | Tự catch trong BaseApiServlet | Tomcat nhúng tay |
| JaxRsMain | `@Path/@GET/@POST` | Jackson tự động | ExceptionMapper (1 class/loại lỗi) | Tomcat nhúng + Jersey servlet |
| SpringMvcMain | `@RequestMapping` | Jackson tự động | `@RestControllerAdvice` | Tomcat nhúng + DispatcherServlet tay |
| SpringBootMain | `@RequestMapping` (tái dùng) | Jackson tự động | `@RestControllerAdvice` (tái dùng) | **0 dòng** — Boot lo hết |

## REST — Idiom lỗi HTTP status

Tất cả REST entry point (WebMain/Servlet/JAX-RS/Spring) dùng chung pattern:
- 200 OK — thành công
- 201 Created — tạo mới thành công
- 400 Bad Request — validation fail, dữ liệu sai
- 404 Not Found — không tìm thấy resource
- 405 Method Not Allowed — sai HTTP method (kèm header `Allow`)
- 409 Conflict — optimistic lock (chỉ SpringFrameworkMain/SpringBootMain)
- 500 Internal Server Error — lỗi hệ thống

Response lỗi luôn dạng: `{"error": "..."}`

## SOAP — 2 nhánh trong project

### JAX-WS / Apache CXF (SoapMain, code-first)
- Viết Java interface `TaiKhoanSoapService` với `@WebService` + `@WebMethod`
- CXF sinh WSDL tự động từ annotation
- Publish qua `Endpoint.publish(address, impl)` — CXF tự dựng Jetty
- Lỗi: `@WebFault` exception → `<soap:Fault>` trong response XML
- JAX-WS bị gỡ khỏi JDK từ Java 11 → bắt buộc dependency ngoài

### Spring-WS (SpringWsMain, contract-first)
- Viết XSD trước (`tai-khoan-ws.xsd`), WSDL sinh runtime từ XSD
- `@Endpoint` + `@PayloadRoot(namespace, localPart)` — dispatch theo NỘI DUNG XML, không theo URL
- JAXB class viết tay (khớp XSD) thay vì codegen plugin
- Lỗi: `@SoapFault` annotation trên exception

### So sánh SOAP vs REST
| Khía cạnh | REST | SOAP |
|-----------|------|------|
| Format | JSON (text, nhẹ) | XML (nặng, strict) |
| Hợp đồng | OpenAPI/Swagger (tùy chọn) | WSDL (bắt buộc, nghiêm ngặt) |
| Routing | URL + HTTP method | 1 URL, action trong body |
| Lỗi | HTTP status code | SOAP Fault (XML structured) |
| Khi nào dùng | Hầu hết API hiện đại | Hệ thống ngân hàng/doanh nghiệp cũ, đối tác yêu cầu WSDL |

## gRPC (GrpcMain, port 8093)

- HTTP/2 nhị phân (protobuf) — không đọc được bằng mắt, nhanh hơn JSON
- Contract `.proto` (contract-first), `protobuf-maven-plugin` sinh code Java lúc build
- 4 loại RPC: unary, server-streaming, client-streaming, bidirectional
- Project demo: 7 unary + 1 server-streaming (`TheoDoiSoDu` — server tự đẩy state mỗi 1s)
- Lỗi: `Status.NOT_FOUND`, `Status.INVALID_ARGUMENT` → `StatusRuntimeException`
- Dùng `grpc-netty-shaded` (tránh xung đột Netty với WebFlux)

## WebSocket (WebSocketMain, port 8091)

- Kết nối 2 chiều thật — server TỰ push mà không cần client hỏi
- `@ServerEndpoint("/ws/tai-khoan/{soTaiKhoan}")` + `@OnOpen/@OnMessage/@OnClose/@OnError`
- Client kết nối → server đẩy state ngay; client gửi "refresh" → server query DB và đẩy lại
- Khác REST: không có request/response rõ ràng, phải tự thiết kế "protocol" trên message
- Static field `taiKhoanService` vì container tạo instance mới cho mỗi connection

## GraphQL (GraphQlMain, port 8092)

- 1 endpoint HTTP POST `/graphql` duy nhất
- Schema SDL (`schema.graphqls`): `type Query`, `type Mutation`, `type TaiKhoan`
- `@Controller` + `@QueryMapping` / `@MutationMapping` + `@Argument`
- Client tự chọn field cần lấy → tránh over-fetching
- Lỗi: HTTP luôn 200, lỗi trong `"errors"[]` + `extensions.classification`
- `DataFetcherExceptionResolverAdapter` map exception → GraphQL error

## Quarkus (quarkus-app/, port 8094)

- Project Maven **hoàn toàn tách biệt** (pom.xml riêng, classpath riêng)
- Build-time DI (augmentation) — khác Spring (runtime reflection)
- Panache entity: `TaiKhoan.persist()` / `TaiKhoan.listAll()` ngay trên instance
- JAX-RS annotation (tương thích, qua `quarkus-rest`)
- Dev mode: `mvn quarkus:dev` (hot-reload khi sửa code)
- Đóng gói: `target/quarkus-app/quarkus-run.jar` (thin jar + lib/)
