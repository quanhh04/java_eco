# Quản lý khách hàng mở tài khoản chứng khoán

Ứng dụng Java SE, build bằng Maven, quản lý khách hàng và tài khoản chứng khoán họ mở. Dữ liệu lưu trên PostgreSQL qua JDBC. 14 entry point (13 trong project chính + Quarkus tách riêng) dùng chung tầng nghiệp vụ, phủ gần trọn hệ sinh thái Java xây web server "từ xưa đến nay":
- Console (`Main`)
- Web service Java SE thuần (`WebMain`, dùng `com.sun.net.httpserver` có sẵn trong JDK, tự viết router + JSON, không thêm framework)
- Jakarta EE — Servlet thuần (`ServletMain`) và JAX-RS/Jersey (`JaxRsMain`), mỗi cái một entry point + port riêng để dễ so sánh, cùng chạy trên embedded Tomcat, không dùng Spring
- JAX-WS/SOAP bằng Apache CXF (`SoapMain`) — expose nghiệp vụ tài khoản chứng khoán qua SOAP, kiểu core-banking service mà hệ thống ngân hàng cũ hay dùng
- Spring ecosystem — Spring MVC (`SpringMvcMain`), Spring Boot (`SpringBootMain`), Spring WebFlux (`SpringWebFluxMain`), Spring Web Services/SOAP (`SpringWsMain`), mỗi cái một entry point + port riêng để so sánh
- Spring Framework thuần đầy đủ, không Boot (`SpringFrameworkMain`) — ghép IoC, MVC, Transaction Management, AOP, Security/Filter, Hibernate setup thủ công thành 1 "mini Spring Framework" để hiểu bản chất trước khi dùng Spring Boot
- Giao thức khác — gRPC (`GrpcMain`, HTTP/2 nhị phân + server-streaming), WebSocket (`WebSocketMain`, 2 chiều thật sự), GraphQL (`GraphQlMain`, 1 endpoint, client tự chọn field) — mỗi cái minh họa 1 mô hình giao tiếp mà REST/SOAP ở trên không làm được
- Framework khác không Spring — Quarkus (`quarkus-app/`, project Maven **độc lập hoàn toàn**, không chung classpath/pom.xml với 13 entry point kia), build-time DI + Panache, chạy `mvn quarkus:dev`

## Luồng khởi chạy

Mười ba entry point trong project chính (13 port khác nhau, chạy riêng lẻ hoặc cùng lúc đều được), dùng chung `service`/`repository`/`model`/`config`, chỉ khác lớp "cửa vào" (input/output) — cộng thêm Quarkus (`quarkus-app/`) là project Maven tách biệt hoàn toàn, xem mục riêng bên dưới:

```
Console:      Main.main()             -> ConsoleMenu (đọc Scanner, in System.out)
                                                 |
Web SE:       WebMain.main()           -> WebServer (HttpServer, :8080) -> Router -> KhachHangHandler / TaiKhoanHandler
                                                 |                                   (JSON tự viết qua HttpExchange)
Servlet:      ServletMain.main()       -> Tomcat nhúng (:8083) -> KhachHangServlet / TaiKhoanServlet
                                                 |                (JSON tự viết, tái dùng JsonWriter/JsonParser/RequestBody)
JAX-RS:       JaxRsMain.main()         -> Tomcat nhúng (:8084) -> Jersey ServletContainer -> KhachHangResource/TaiKhoanResource
                                                 |                (JAX-RS annotation, Jackson tự serialize, ExceptionMapper)
JAX-WS:       SoapMain.main()          -> CXF tự dựng Jetty (:8085) -> TaiKhoanSoapServiceImpl
                                                 |                (SOAP/WSDL qua Endpoint.publish(), lỗi -> SOAP Fault)
Spring MVC:   SpringMvcMain.main()     -> Tomcat nhúng (:8086) tự dựng tay -> DispatcherServlet -> KhachHangController
                                                 |                (@RestController, @RestControllerAdvice xử lý lỗi)
Spring Boot:  SpringBootMain.main()    -> Tomcat tự động (:8087) -> [tái dùng NGUYÊN controller của Spring MVC ở trên]
                                                 |                (0 dòng code hạ tầng — đây chính là "cái wrapper")
WebFlux:      SpringWebFluxMain.main() -> Netty (:8088) -> KhachHangReactiveController (Mono/Flux bọc service blocking)
                                                 |
Spring-WS:    SpringWsMain.main()      -> Tomcat nhúng (:8089) tự dựng tay -> TaiKhoanEndpoint (@PayloadRoot, contract-first)
                                                 |                (route theo nội dung XML, không theo URL)
Mini Spring:  SpringFrameworkMain.main() -> Tomcat nhúng (:8090) tự dựng tay -> DelegatingFilterProxy (Security)
                                                 |                -> DispatcherServlet -> TaiKhoanFwController
                                                 |                -> TaiKhoanFwService (@Transactional, bọc AOP logging)
                                                 |                -> TaiKhoanDao (EntityManager/Hibernate, KHÔNG qua JDBC repo)
gRPC:         GrpcMain.main()          -> Server gRPC thuần (:8093) -> TaiKhoanGrpcServiceImpl
                                                 |                (HTTP/2 nhị phân, protobuf; unary + server-streaming TheoDoiSoDu)
WebSocket:    WebSocketMain.main()     -> Tomcat nhúng (:8091) tự dựng tay -> TaiKhoanWebSocketEndpoint
                                                 |                (2 chiều thật: server tự push khi client gửi "refresh")
GraphQL:      GraphQlMain.main()       -> Tomcat tự động (:8092) -> TaiKhoanGraphQlController
                                                 |                (1 endpoint POST /graphql, client tự chọn field cần lấy)
                                (cả 13 đi vào cùng 1 chỗ — riêng SpringFrameworkMain đi qua Hibernate thay vì JDBC repo)
                                                 v
                  KhachHangService / TaiKhoanService  (nghiệp vụ, validate)
                                                 v
              KhachHangRepositoryImpl / TaiKhoanRepositoryImpl (JDBC)
                                                 v
                     PostgreSQL (qua DatabaseConfig)

Quarkus (project riêng, quarkus-app/, KHÔNG chung sơ đồ trên):
  TaiKhoanResource (:8094) -> TaiKhoan (Panache entity, persist() ngay trên entity)
                           -> JDBC trực tiếp (validate tuổi khách hàng, project riêng không tái dùng KhachHangService)
                           -> PostgreSQL (CÙNG database thật, đọc credentials thủ công trong application.properties)
```

Chạy cái nào cũng ra cùng một nghiệp vụ, cùng một dữ liệu — khác biệt duy nhất là lớp giao diện. Sửa business rule chỉ cần sửa ở `service/`, tất cả lối vào đều nhận được thay đổi (riêng `SpringFrameworkMain` có `TaiKhoanFwService` riêng dùng Hibernate, và Quarkus có `TaiKhoan` Panache riêng — xem mục bên dưới). `ServletMain`/`JaxRsMain`/`SpringMvcMain`/`SpringBootMain`/`SpringWebFluxMain`/`SpringFrameworkMain` cố tình tách port riêng và cùng route `/api/tai-khoan` — chạy nhiều cái cùng lúc, đổi port trong curl là so sánh được ngay nhiều cách viết cho cùng 1 API. Riêng `SpringFrameworkMain` chỉ expose `/api/tai-khoan` (không có `/api/khach-hang` — phạm vi chỉ tài khoản, giống cách đã giảm scope cho `SoapMain`), khách hàng vẫn được validate qua `KhachHangService` JDBC tái dùng nguyên. gRPC/WebSocket/GraphQL/Quarkus cũng chỉ expose nghiệp vụ tài khoản chứng khoán, cùng lý do giảm scope.

## Cấu trúc

```
pom.xml                      Khai báo dependency (driver PostgreSQL) và plugin build
src/main/java/com/demo/securities/
  Main.java                  Điểm khởi chạy console
  WebMain.java               Điểm khởi chạy web service Java SE thuần
  ServletMain.java           Điểm khởi chạy Jakarta EE - Servlet thuần, tự bootstrap Tomcat nhúng (port 8083)
  JaxRsMain.java             Điểm khởi chạy Jakarta EE - JAX-RS/Jersey, tự bootstrap Tomcat nhúng (port 8084)
  SoapMain.java              Điểm khởi chạy JAX-WS/SOAP (Apache CXF), tự publish qua Endpoint.publish() (port 8085)
  SpringMvcMain.java         Điểm khởi chạy Spring MVC (không Boot), tự dựng Tomcat + DispatcherServlet tay (port 8086)
  SpringBootMain.java        Điểm khởi chạy Spring Boot (wrapper) — tái dùng controller của SpringMvcMain (port 8087)
  SpringWebFluxMain.java     Điểm khởi chạy Spring WebFlux (reactive), chạy trên Netty (port 8088)
  SpringWsMain.java          Điểm khởi chạy Spring Web Services (SOAP contract-first), tự dựng Tomcat tay (port 8089)
  SpringFrameworkMain.java   Điểm khởi chạy "mini Spring Framework" đầy đủ — IoC/MVC/TX/AOP/Security/Hibernate, không Boot (port 8090)
  GrpcMain.java              Điểm khởi chạy gRPC — Server thuần (grpc-netty-shaded), không qua Tomcat/servlet (port 8093)
  WebSocketMain.java         Điểm khởi chạy WebSocket — Tomcat nhúng tự dựng tay + WsSci (port 8091)
  GraphQlMain.java           Điểm khởi chạy GraphQL — Spring Boot + spring-boot-starter-graphql (port 8092)
  model/                     KhachHang, TaiKhoanChungKhoan, enum GioiTinh/LoaiTaiKhoan/TrangThaiTaiKhoan
  exception/                 ValidationException, NotFoundException, DuplicateException, DataAccessException
  util/                      Validator (kiểm tra dữ liệu), IdGenerator (sinh mã)
  config/                    DatabaseConfig (đọc db.properties, mở Connection)
  repository/                Interface + impl JDBC (PostgreSQL)
  service/                   KhachHangService, TaiKhoanService (nghiệp vụ, ràng buộc — dùng chung cho tất cả giao diện)
  ui/                        ConsoleMenu (giao diện dòng lệnh)
  tool/                      SchemaInitializer (chạy schema.sql để tạo bảng)
  web/                       Hạ tầng HTTP tự viết (dùng cho WebMain): JsonWriter/JsonParser (json/), Router (route + mã lỗi HTTP), HttpUtil, RequestBody
  web/handler/                KhachHangHandler, TaiKhoanHandler (route REST, dùng lại service hiện có)
  servlet/                   BaseApiServlet, KhachHangServlet, TaiKhoanServlet, ServletHttpUtil (raw HttpServlet, tái dùng JsonWriter/JsonParser/RequestBody của web/)
  jaxrs/                     KhachHangResource, TaiKhoanResource, SecuritiesApplication (JAX-RS/Jersey), dto/ (record DTO), *ExceptionMapper (map exception -> HTTP status)
  soap/                      TaiKhoanSoapService (SEI), TaiKhoanSoapServiceImpl, TaiKhoanSoapDto, TaiKhoanFaultException/TaiKhoanFaultInfo (SOAP Fault thay vì mã HTTP)
  spring/                    AppConfig (@Bean service/repo), KhachHangController, TaiKhoanController, GlobalExceptionHandler (@RestControllerAdvice), dto/ — dùng chung giữa SpringMvcMain và SpringBootMain
  springmvc/                 WebMvcConfig (@EnableWebMvc) — package RIÊNG, chỉ SpringMvcMain biết tới, không để SpringBootMain quét trúng
  springflux/                KhachHangReactiveController, TaiKhoanReactiveController (Mono/Flux, bọc service blocking qua Schedulers.boundedElastic)
  springws/                  TaiKhoanEndpoint (@Endpoint, @PayloadRoot), WsConfig, request/response JAXB tay viết khớp XSD, TaiKhoanFaultException (@SoapFault)
  springfw/                  entity/TaiKhoanEntity (JPA, có @Version cho Optimistic Lock), dao/TaiKhoanDao (EntityManager thuần), service/TaiKhoanFwService (@Transactional, có chuyenTien demo rollback) + OptimisticLockDemoService (dùng chung với SpringBootMain), aop/LoggingAspect, config/JpaConfig+AopConfig+SecurityConfig, web/TaiKhoanFwController
  springboot/                OptimisticLockDemoController — riêng cho SpringBootMain, tái dùng TaiKhoanDao/OptimisticLockDemoService của springfw/ qua @Import
  grpc/                      TaiKhoanGrpcServiceImpl (dùng lại TaiKhoanService), + code gen tự động từ .proto (KHÔNG commit, nằm trong target/generated-sources/protobuf)
  websocket/                 TaiKhoanWebSocketEndpoint (@ServerEndpoint, static field tham chiếu TaiKhoanService)
  graphql/                   TaiKhoanGraphQlController (@Controller, @QueryMapping/@MutationMapping), GraphQlExceptionResolver (DataFetcherExceptionResolver)
src/main/proto/tai_khoan.proto        Hợp đồng gRPC (contract-first, sinh code lúc build qua protobuf-maven-plugin)
src/main/resources/graphql/schema.graphqls  SDL contract-first cho GraphQL (Query/Mutation/type TaiKhoan)
src/main/resources/tai-khoan-ws.xsd  XSD contract-first cho Spring-WS (viết trước, WSDL sinh động lúc runtime)
schema.sql                   Câu lệnh tạo schema/bảng
db.properties                Thông tin kết nối DB (không commit — xem db.properties.example)
quarkus-app/                 Project Maven RIÊNG (pom.xml/classpath/build lifecycle tách biệt hoàn toàn khỏi project chính)
  pom.xml                    quarkus-bom, extension quarkus-rest/quarkus-rest-jackson/quarkus-hibernate-orm-panache/quarkus-jdbc-postgresql
  src/main/java/com/demo/quarkus/
    entity/TaiKhoan.java     Panache entity (persist()/list() ngay trên instance, không qua DAO riêng)
    resource/TaiKhoanResource.java  JAX-RS chuẩn (quarkus-rest tương thích API), validate tuổi khách hàng qua JDBC trực tiếp
    dto/                     TaiKhoanDto, OpenTaiKhoanRequest, SoTienRequest (record)
  src/main/resources/application.properties  Thông tin kết nối DB thật (không commit — xem application.properties.example), port quarkus.http.port=8094
```

## Cài đặt

Yêu cầu: JDK 21+ và Maven đã cài, có trong PATH (`mvn -version` để kiểm tra).

1. Copy `db.properties.example` thành `db.properties`, điền thông tin kết nối:
   ```
   db.url=jdbc:postgresql://<host>:5432/<database>
   db.user=<user>
   db.password=<password>
   db.schema=<schema>
   ```

2. Tạo bảng trên DB (chỉ cần chạy một lần):
   ```
   mvn compile exec:java -Dexec.mainClass=com.demo.securities.tool.SchemaInitializer
   ```

3. Nếu muốn chạy Quarkus (`quarkus-app/`, project riêng): copy `quarkus-app/src/main/resources/application.properties.example` thành `application.properties` cùng thư mục, điền cùng thông tin kết nối DB.

## Chạy console app

```
mvn compile exec:java
```

Hoặc đóng gói jar chạy độc lập (đã gộp sẵn driver PostgreSQL, `Main-Class` trong manifest trỏ tới console):
```
mvn package
java -jar target/quan-ly-tai-khoan-chung-khoan.jar
```

## Chạy web service

Vì `pom.xml` mặc định `exec.mainClass=com.demo.securities.Main`, phải override khi muốn chạy web:

Git Bash / Linux / macOS:
```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.WebMain -Dserver.port=8080
```

PowerShell (bắt buộc quote toàn bộ tham số `-D`, nếu không PowerShell tách sai ở dấu `.`):
```powershell
mvn compile exec:java "-Dexec.mainClass=com.demo.securities.WebMain" "-Dserver.port=8080"
```

Hoặc chạy từ jar đã đóng gói bằng cách override main class qua `-cp` (không dùng `java -jar`, vì manifest của jar trỏ tới `Main`):
```
mvn package
java -Dserver.port=8080 -cp target/quan-ly-tai-khoan-chung-khoan.jar com.demo.securities.WebMain
```

Cả hai cách đều phải chạy từ thư mục gốc project (nơi có `db.properties`). Mặc định cổng `8080`, đổi bằng `-Dserver.port=`.

### API

Khách hàng:
| Method | Path | Body |
|---|---|---|
| GET | `/api/khach-hang` (hỗ trợ `?ten=`) | |
| GET | `/api/khach-hang/{id}` | |
| POST | `/api/khach-hang` | `hoTen, ngaySinh (yyyy-MM-dd), gioiTinh, soCCCD, soDienThoai, email, diaChi` |
| PUT | `/api/khach-hang/{id}` | như trên (trừ soCCCD) |
| DELETE | `/api/khach-hang/{id}` | |

Tài khoản chứng khoán:
| Method | Path | Body |
|---|---|---|
| GET | `/api/tai-khoan` (hỗ trợ `?khachHangId=`) | |
| GET | `/api/tai-khoan/{so}` | |
| POST | `/api/tai-khoan` | `khachHangId, loaiTaiKhoan, soDuBanDau` |
| POST | `/api/tai-khoan/{so}/khoa`, `/mo-khoa`, `/dong` | |
| POST | `/api/tai-khoan/{so}/nap`, `/rut` | `soTien` |

Ví dụ:
```
curl -X POST -H "Content-Type: application/json" \
  -d '{"hoTen":"Nguyen Van A","ngaySinh":"1990-05-20","gioiTinh":"NAM","soCCCD":"012345678901","soDienThoai":"0912345678","email":"a@example.com","diaChi":"Ha Noi"}' \
  http://localhost:8080/api/khach-hang
```

Mã lỗi trả về JSON dạng `{"error": "..."}`: `400` (dữ liệu/JSON không hợp lệ, vi phạm nghiệp vụ), `404` (không tìm thấy), `405` (sai method, kèm header `Allow`), `500` (lỗi hệ thống).

## Chạy Jakarta EE — Servlet thuần và JAX-RS (2 entry point, 2 port riêng)

Không dùng `tomcat7-maven-plugin`/WAR: cả hai tự viết code khởi động Tomcat nhúng (`org.apache.tomcat.embed:tomcat-embed-core`) — vẫn đạt mục tiêu "một lệnh chạy, không cần cài Tomcat riêng, không đóng gói WAR tay", nhưng dùng đúng namespace `jakarta.servlet` hiện đại (Tomcat 11) thay vì plugin cũ chỉ hỗ trợ `javax.servlet`.

### Servlet thuần (`ServletMain`, mặc định port 8083)

Tự viết `doGet`/`doPost`/`doPut`/`doDelete`, tự đọc/ghi JSON tay (tái dùng nguyên `JsonWriter`/`JsonParser`/`RequestBody` đã viết cho `WebMain`), tự bắt exception map sang mã HTTP — thấy rõ toàn bộ phần "việc chân tay" mà framework thường làm giúp.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.ServletMain -Dserver.port=8083
```
PowerShell: `mvn compile exec:java "-Dexec.mainClass=com.demo.securities.ServletMain" "-Dserver.port=8083"`

### JAX-RS / Jersey (`JaxRsMain`, mặc định port 8084)

Routing bằng annotation (`@Path`, `@GET`, `@POST`...), JSON tự động qua Jackson (không cần `JsonWriter`/`JsonParser` tay), lỗi xử lý qua `ExceptionMapper` (mỗi loại exception một class nhỏ, JAX-RS tự chọn mapper khớp nhất theo type) thay vì try/catch thủ công.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.JaxRsMain -Dserver.port=8084
```
PowerShell: `mvn compile exec:java "-Dexec.mainClass=com.demo.securities.JaxRsMain" "-Dserver.port=8084"`

### Cùng API shape, đổi port là so sánh được

Cả hai (và cả `WebMain`) đều expose đúng route `/api/khach-hang`, `/api/tai-khoan` như bảng ở trên — chạy cả 3 cùng lúc (3 port khác nhau), chỉ đổi port trong curl để thấy cùng 1 nghiệp vụ được 3 công nghệ khác nhau phục vụ:
```
curl -X POST http://localhost:8080/api/khach-hang -d '{...}'   # WebMain: tay viết Router + JSON (Java SE thuần)
curl -X POST http://localhost:8083/api/khach-hang -d '{...}'   # ServletMain: tay viết Servlet, tái dùng JSON đã có
curl -X POST http://localhost:8084/api/khach-hang -d '{...}'   # JaxRsMain: framework (Jersey) lo routing + JSON + lỗi
```

## Chạy JAX-WS (SOAP) — Apache CXF

`SoapMain` chỉ expose nghiệp vụ **tài khoản chứng khoán** (mở/khóa/mở khóa/đóng/nạp/rút/truy vấn) qua SOAP — kiểu core-banking interface mà hệ thống ngân hàng cũ hay dùng để lộ ra cho hệ thống khác gọi vào. Không expose CRUD khách hàng qua SOAP (giữ scope hợp lý).

JAX-WS (`javax.xml.ws`) từng có sẵn trong JDK 6–10 nhưng **bị gỡ khỏi JDK từ Java 11** — khác với Servlet/JAX-RS ở trên, bắt buộc phải thêm dependency ngoài (`jakarta.xml.ws-api`, `jakarta.jws-api`, CXF). Chọn Apache CXF thay vì Metro (JAX-WS RI) vì phổ biến hơn trong thực tế doanh nghiệp/ngân hàng. Publish qua đúng API chuẩn `jakarta.xml.ws.Endpoint.publish()` — CXF tự nhận (đăng ký qua `META-INF/services/jakarta.xml.ws.spi.Provider`) và tự dựng embedded Jetty, không cần Tomcat/servlet nào cho phần này.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.SoapMain -Dserver.port=8085
```
PowerShell: `mvn compile exec:java "-Dexec.mainClass=com.demo.securities.SoapMain" "-Dserver.port=8085"`

Xem WSDL: `curl http://localhost:8085/tai-khoan-soap?wsdl`

Gọi thử operation `moTaiKhoan` bằng SOAP envelope thô (không có thư viện client, gọi tay để thấy đúng bản chất SOAP):
```
curl -X POST -H "Content-Type: text/xml; charset=utf-8" -H 'SOAPAction: ""' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://soap.securities.demo.com/">
        <soapenv:Body>
          <tns:moTaiKhoan>
            <khachHangId>KH0001</khachHangId>
            <loaiTaiKhoan>CO_SO</loaiTaiKhoan>
            <soDuBanDau>2500000</soDuBanDau>
          </tns:moTaiKhoan>
        </soapenv:Body>
      </soapenv:Envelope>' \
  http://localhost:8085/tai-khoan-soap
```

Lỗi nghiệp vụ (số dư không đủ, tài khoản không tồn tại...) trả về **SOAP Fault** (`<soap:Fault>` kèm `<detail><TaiKhoanFault><message>...`) — cơ chế báo lỗi hoàn toàn khác REST (không có khái niệm mã trạng thái 400/404, JAX-WS dùng exception được đánh dấu `@WebFault` map thẳng vào XML).

## Chạy Spring ecosystem (4 entry point, 4 port riêng)

Yêu cầu build: cờ compile `-parameters` (đã bật sẵn trong `pom.xml` qua `maven-compiler-plugin`) — Spring cần đọc tên tham số qua reflection cho `@RequestParam`/`@PathVariable` không khai tên tường minh.

### Spring MVC — không Spring Boot (`SpringMvcMain`, port 8086)

Tự dựng `AnnotationConfigWebApplicationContext` + `DispatcherServlet` + Tomcat nhúng bằng tay (`context.register(...)` tường minh — `context.scan()` package không hoạt động ổn định trên Spring Framework 7 nên đã đổi qua đăng ký từng class), y hệt pattern `ServletMain`/`JaxRsMain`, chỉ khác "servlet" giờ là của Spring.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.SpringMvcMain -Dserver.port=8086
```

### Spring Boot — "cái wrapper" (`SpringBootMain`, port 8087)

Tái dùng NGUYÊN `KhachHangController`/`TaiKhoanController`/`GlobalExceptionHandler`/`AppConfig` từ `SpringMvcMain` ở trên — không một dòng code Tomcat/DispatcherServlet nào, chỉ `@SpringBootApplication` + `SpringApplicationBuilder(...).web(WebApplicationType.SERVLET).run(...)`. Đây chính là điểm so sánh trực tiếp: cùng controller, khác hẳn lượng code khởi động.

Ngoài route JDBC gốc, entry point này còn `@Import` thêm `TaiKhoanDao`/`OptimisticLockDemoService` (tái dùng NGUYÊN từ `SpringFrameworkMain`) cộng 1 endpoint `POST /api/tai-khoan/{so}/demo-optimistic-lock` — bật `spring-boot-starter-data-jpa`, cấu hình qua `spring.datasource.*`/`spring.jpa.*` properties (set trong `main()`, đọc từ `db.properties`), **không có `JpaConfig` thủ công nào** — đúng phép so sánh trực tiếp với mục Optimistic Lock của `SpringFrameworkMain` bên dưới: cùng entity, cùng service, khác duy nhất cách EntityManagerFactory/JpaTransactionManager được tạo ra.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.SpringBootMain -Dserver.port=8087
```

### Spring WebFlux — Reactive (`SpringWebFluxMain`, port 8088)

Controller trả `Mono<T>`/`Flux<T>`, chạy trên **Netty** (không phải Tomcat). Bọc `KhachHangService`/`TaiKhoanService` (JDBC blocking) qua `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())` — "reactive wrapper trên DAO blocking cũ", không đổi sang R2DBC, đúng kiểu 1 team thêm WebFlux trên nền code cũ trong thực tế.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.SpringWebFluxMain -Dserver.port=8088
```

### Spring Web Services — SOAP contract-first (`SpringWsMain`, port 8089)

Khác nhánh JAX-WS/CXF ở trên: đây đúng tinh thần **contract-first của Spring-WS** — tự viết XSD trước (`src/main/resources/tai-khoan-ws.xsd`), WSDL sinh động lúc runtime từ XSD đó, `@Endpoint` dispatch theo `@PayloadRoot` (namespace + tên phần tử gốc của XML request) chứ không theo URL. Giản lược đã ghi rõ trong code: hand-write class JAXB khớp XSD thay vì dùng plugin codegen (`jaxb2-maven-plugin`/xjc) như dự án thật thường làm.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.SpringWsMain -Dserver.port=8089
```

Xem WSDL: `curl http://localhost:8089/tai-khoan-ws/tai-khoan-ws.wsdl`

Gọi thử `MoTaiKhoanRequest`:
```
curl -X POST -H "Content-Type: text/xml; charset=utf-8" -H 'SOAPAction: ""' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://spring-ws.securities.demo.com/taikhoan">
        <soapenv:Body>
          <tns:MoTaiKhoanRequest>
            <khachHangId>KH0001</khachHangId>
            <loaiTaiKhoan>CO_SO</loaiTaiKhoan>
            <soDuBanDau>1500000</soDuBanDau>
          </tns:MoTaiKhoanRequest>
        </soapenv:Body>
      </soapenv:Envelope>' \
  http://localhost:8089/tai-khoan-ws
```

### Gotcha đã gặp khi build nhánh Spring (đáng lưu ý nếu bạn tự làm lại)

- **Xung đột version `jackson-annotations`**: Jersey (JAX-RS) kéo bản 2.19.1, Spring Boot 4/Spring 7 cần bản 2.21 (có class mới hơn) — Maven mediation ưu tiên nhầm bản cũ hơn gây `NoClassDefFoundError`. Sửa bằng cách khai tường minh `jackson-annotations:2.21` làm dependency trực tiếp trong `pom.xml` để nó luôn thắng.
- **`context.scan("com.demo.securities.spring")` không hoạt động** trên Spring Framework 7 trong setup thủ công này (controller không được khởi tạo, không lỗi, không log) — chuyển sang `context.register(KhachHangController.class, ...)` liệt kê từng class, ổn định hơn cho kiểu cấu hình thủ công này.
- **`ClassNotFoundException` khi Spring-WS tự tra cứu "default strategy"** (`SaajSoapMessageFactory`, `SoapMessageDispatcher`) qua `ClassUtils.forName` phản chiếu bên trong Tomcat nhúng — Tomcat tự tạo `WebappClassLoader` cho context không tự động kế thừa đúng classloader ứng dụng. Sửa bằng `ctx.setParentClassLoader(SpringWsMain.class.getClassLoader())` sau khi `addContext(...)`, cộng thêm khai tường minh bean `WebServiceMessageFactory` để né hẳn 1 bước tra cứu reflection.

## Chạy Spring Framework thuần — "mini Spring Framework" (`SpringFrameworkMain`, port 8090)

Entry point sâu nhất trong project: ghép đủ **IoC/DI, DispatcherServlet/MVC, Transaction Management, AOP, Security/Filter, và Hibernate setup thủ công** — không dùng bất kỳ `spring-boot-starter-*` nào — để thấy rõ Spring Framework hoạt động ra sao ở tầng nền tảng, trước khi dùng Spring Boot. Phạm vi: chỉ nghiệp vụ **tài khoản chứng khoán** (`/api/tai-khoan`), qua tầng persistence **Hibernate/JPA riêng** (không dùng lại `TaiKhoanRepositoryImpl` JDBC) — khách hàng vẫn validate qua `KhachHangService` JDBC tái dùng nguyên.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.SpringFrameworkMain -Dserver.port=8090
```

Thêm 1 nghiệp vụ mới để demo Transaction Management thật: `POST /api/tai-khoan/{so}/chuyen-den/{soDich}` (body `{"soTien": ...}`) — chuyển tiền giữa 2 tài khoản trong 1 `@Transactional`. Nếu tài khoản đích không tồn tại/không hoạt động, phần trừ tiền tài khoản nguồn (đã làm trong cùng transaction) **rollback tự động** — test bằng cách chuyển tới 1 mã tài khoản không có thật rồi truy vấn lại tài khoản nguồn, số dư không đổi.

`GET /api/tai-khoan/**` công khai; `POST`/`PUT`/`DELETE` (kể cả `chuyen-den`) yêu cầu HTTP Basic Auth — user in-memory `admin` / `admin123` (chỉ để học cơ chế filter chain, không dùng cho production):
```
curl http://localhost:8090/api/tai-khoan                                    # GET công khai, không cần auth
curl -X POST http://localhost:8090/api/tai-khoan/TK000001/nap -d '{...}'    # 401 nếu thiếu auth
curl -u admin:admin123 -X POST http://localhost:8090/api/tai-khoan/TK000001/nap -d '{...}'  # OK
```

### Bảng ánh xạ: phần thủ công ở đây ↔ Spring Boot tự động hóa hộ

| Mảnh Spring | Cấu hình thủ công trong `SpringFrameworkMain` | Spring Boot thay bằng |
|---|---|---|
| Embedded server | Tự viết `Tomcat`, `addContext`, `addServlet`, `loadOnStartup` (~15 dòng) | `spring-boot-starter-web` — tự động, 0 dòng |
| DispatcherServlet | Tự `new DispatcherServlet(context)`, đăng ký servlet mapping tay | Tự động đăng ký qua auto-configuration |
| IoC Container | Tự `new AnnotationConfigWebApplicationContext()`, `context.register(...)` liệt kê từng class | `@SpringBootApplication` tự quét (`@ComponentScan`) |
| Hibernate/JPA | `JpaConfig`: tự tạo `DataSource`, `LocalContainerEntityManagerFactoryBean`, set `persistenceProviderClass`, `PersistenceAnnotationBeanPostProcessor` tay (~40 dòng) | `spring-boot-starter-data-jpa` — tự cấu hình hết từ `application.properties` |
| Transaction Management | `JpaTransactionManager` bean tay + `@EnableTransactionManagement` tay | Tự động bật kèm `spring-boot-starter-data-jpa` |
| AOP | `AopConfig` với `@EnableAspectJAutoProxy` tay, tự thêm dependency `aspectjweaver` | Tự bật khi có `spring-boot-starter-aop` (hoặc đã bật sẵn ở nhiều starter khác) |
| Security | `SecurityConfig` (`SecurityFilterChain`, `UserDetailsService`) + tự đăng ký `DelegatingFilterProxy` qua `FilterDef`/`FilterMap` tay (~15 dòng riêng phần filter) | `spring-boot-starter-security` — tự đăng ký filter chain, chỉ cần viết `SecurityFilterChain` bean |
| Version quản lý dependency | Tự tìm/ghim từng version tương thích tay (đã dính 2 lỗi conflict version khi build) | BOM `spring-boot-dependencies` tự quản lý version nhất quán toàn bộ |
| Classloader trong Tomcat nhúng | Tự `ctx.setParentClassLoader(...)` để tránh `ClassNotFoundException` (gặp lại lỗi này lần 3 trong project) | Không gặp — Boot dùng cơ chế khởi động khác, không tự dựng Tomcat nhúng kiểu `addContext` trần trụi |

### Gotcha mới gặp khi build (khác 2 lỗi đã ghi ở trên)

**Filter khởi tạo trước Servlet trong vòng đời Tomcat.** `DelegatingFilterProxy` (Security) init trước `DispatcherServlet` lúc `tomcat.start()`. Nếu không tự `context.setServletContext(ctx.getServletContext())` + `context.refresh()` tường minh TRƯỚC khi đăng ký cả filter lẫn servlet, `DelegatingFilterProxy` sẽ là bên đầu tiên đụng tới context và tự trigger `refresh()` — nhưng lúc đó chưa có `ServletContext` gắn vào, khiến `@EnableWebMvc` (`resourceHandlerMapping`) báo lỗi "No ServletContext set". Sửa bằng cách tự set `ServletContext` + `refresh()` ngay sau `addContext(...)`, trước khi đăng ký filter/servlet — cả 2 sau đó chỉ thấy context đã active và dùng lại, không refresh lần nữa.

### Demo Optimistic Lock (`SpringFrameworkMain` port 8090 và `SpringBootMain` port 8087)

`TaiKhoanEntity` có thêm field `@Version private long version;` (cột `version` trong `tai_khoan_chung_khoan`) — Hibernate tự thêm `AND version = ?` vào câu `UPDATE` và tự tăng version lên 1 mỗi lần flush; nếu `UPDATE` khớp 0 dòng (ai đó đã sửa/commit trước, version DB đã khác) thì ném `OptimisticLockException` thay vì âm thầm ghi đè (lost update) — không cần code tay kiểm tra version. Mọi endpoint ghi hiện có (`nap`, `rut`, `khoa`...) đã tự động được bảo vệ, không đổi hành vi khi không có tranh chấp.

`OptimisticLockDemoService.demoXungDot(...)` (dùng chung cho cả 2 entry point) tái hiện xung đột **tất định** (không cần 2 thread/race timing thật, luôn đúng 1 kịch bản, không flaky) bằng 2 `EntityManager` riêng biệt trong cùng 1 request: "phiên A" (do `@Transactional` của Spring quản lý) đọc tài khoản rồi giữ trong bộ nhớ; "phiên B" (tự mở `EntityManager` + transaction riêng qua `EntityManagerFactory`, không liên quan transaction của Spring) đọc lại CÙNG tài khoản, sửa, commit ngay — mô phỏng "1 request khác đã thắng trước". Phiên A sửa tiếp trên entity cũ (version chưa đổi trong bộ nhớ) — lúc `@Transactional` commit sau khi method return, Hibernate phát hiện version lệch và ném exception.

```
curl -u admin:admin123 -X POST http://localhost:8090/api/tai-khoan/TK000001/demo-optimistic-lock   # SpringFrameworkMain
curl -X POST http://localhost:8087/api/tai-khoan/TK000001/demo-optimistic-lock                       # SpringBootMain (không cần Basic Auth)
```

`GlobalExceptionHandler` (dùng chung, `com.demo.securities.spring`) bắt `org.springframework.orm.ObjectOptimisticLockingFailureException` → trả **409 Conflict** — idiom lỗi thứ 8 trong project, khác `400`/`404`/`500` đã có: báo đúng bản chất "xung đột dữ liệu", không phải "dữ liệu sai" hay "lỗi hệ thống". Gọi thử sẽ thấy: response 409 cho phiên A, nhưng số dư tài khoản vẫn tăng đúng 1 lần (từ phiên B) — chứng minh không có lost update, phiên A bị từ chối sạch sẽ thay vì âm thầm mất.

Lưu ý: cột `version` chỉ được Hibernate/JPA trên 2 nhánh này quản lý — các entry point JDBC thuần (`TaiKhoanRepositoryImpl`) không đọc/ghi cột này, nên optimistic lock chỉ bảo vệ được xung đột giữa các writer đi qua JPA, không bảo vệ nếu có 1 writer JDBC xen vào giữa.

## Chạy gRPC (`GrpcMain`, port 8093)

Nhánh "giao thức khác" đầu tiên: HTTP/2 nhị phân (protobuf) thay vì text (JSON/XML) — hợp đồng viết trước trong `.proto` (`src/main/proto/tai_khoan.proto`), giống tinh thần contract-first của Spring-WS nhưng binary thay vì XML. `protobuf-maven-plugin` sinh code Java từ `.proto` lúc `mvn compile` (vào `target/generated-sources/protobuf/`) — đây là toolchain codegen ĐẦU TIÊN trong project, khác hẳn mọi entry point khác chỉ cần biên dịch `.java` thuần.

Có 1 RPC server-streaming (`TheoDoiSoDu`) mà REST/SOAP/GraphQL ở trên không làm được: server tự đẩy nhiều response cho 1 request duy nhất, không cần client hỏi lại.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.GrpcMain -Dserver.port=8093
```
PowerShell: `mvn compile exec:java "-Dexec.mainClass=com.demo.securities.GrpcMain" "-Dserver.port=8093"`

Không có `grpcurl` cài sẵn nên test bằng cách viết 1 client Java nhỏ dùng chính stub đã generate (`TaiKhoanGrpcServiceGrpc.newBlockingStub(channel)`), gọi `moTaiKhoan`/`napTien`/`rutTien`/`theoDoiSoDu`... Lỗi nghiệp vụ trả về qua `StatusRuntimeException` (`Status.NOT_FOUND`, `Status.INVALID_ARGUMENT`) — idiom lỗi hoàn toàn khác HTTP status hay SOAP Fault.

### Gotcha đã gặp

- **Version `protobuf-java` không phải bản "mới nhất"**: Maven Central liệt kê `protobuf-java` mới nhất là dòng 4.x, nhưng `grpc-protobuf:1.83.0` (bản gRPC dùng ở đây) yêu cầu đúng `protobuf-java:3.25.9` theo POM của chính nó — nếu để Maven tự chọn "mới nhất" sẽ lệch. Kiểm tra trực tiếp POM của `grpc-protobuf` trước khi ghim version, tránh lặp lại kiểu lỗi ghim nhầm đã từng gặp với `jackson-annotations`.
- **Dùng `grpc-netty-shaded`, không phải `grpc-netty` trần**: project đã có `spring-boot-starter-webflux` kéo Netty thật (reactor-netty) vào classpath — nếu gRPC cũng dùng Netty trần sẽ đụng version. Bản "shaded" đóng gói Netty riêng dưới namespace khác, tự cô lập, không xung đột.

## Chạy WebSocket (`WebSocketMain`, port 8091)

Minh họa đúng bản chất **2 chiều thật sự** của WebSocket — khác hẳn REST/gRPC unary (luôn là request rồi mới có response): client kết nối `ws://localhost:8091/ws/tai-khoan/{soTaiKhoan}`, server **tự đẩy ngay** trạng thái hiện tại lúc `@OnOpen`; client gửi message `"refresh"` bất kỳ lúc nào thì server truy vấn lại DB và đẩy state mới — không cần client tự poll như REST.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.WebSocketMain -Dserver.port=8091
```
PowerShell: `mvn compile exec:java "-Dexec.mainClass=com.demo.securities.WebSocketMain" "-Dserver.port=8091"`

JDK không có client WebSocket dòng lệnh sẵn (không như `curl` cho HTTP) — test bằng 1 client Java nhỏ dùng `jakarta.websocket.ClientEndpoint`, kết nối rồi gửi `"refresh"`, xác nhận nhận lại đúng JSON state 2 lần (1 lần lúc mở, 1 lần sau refresh).

### Gotcha đã gặp (nghiêm trọng nhất trong toàn bộ project)

**Tomcat's request Mapper cần ít nhất 1 servlet khớp path thì filter chain (bao gồm `WsFilter` — thứ chặn yêu cầu HTTP Upgrade để nâng cấp lên WebSocket) mới được gọi tới.** Context dựng bằng `addContext("", ...)` không có servlet nào khác ngoài WebSocket → **mọi request kể cả handshake WebSocket hợp lệ đều bị trả 404 trước khi chạm tới `WsFilter`** — không phải lỗi classloader như 3 lần gặp ở các nhánh Spring trước đó (một loại lỗi hoàn toàn mới, mất nhiều vòng debug nhất mới tìm ra). Sửa bằng cách thêm 1 servlet catch-all tầm thường mapped `/*` (chỉ trả 404 cho HTTP thường), đủ để Mapper tìm thấy match và cho request đi tiếp vào filter chain:
```java
Tomcat.addServlet(ctx, "default", new HttpServlet() { ... });
ctx.addServletMappingDecoded("/*", "default");
```

## Chạy GraphQL (`GraphQlMain`, port 8092)

Dùng **Spring GraphQL** (`spring-boot-starter-graphql`) — tái dùng hạ tầng Spring Boot đã ổn định trong project (khác gRPC/WebSocket ở trên phải tự dựng server tay vì không có sẵn tầng Spring cho giao thức đó). Toàn bộ nghiệp vụ đi qua **1 endpoint HTTP POST `/graphql` duy nhất** — hợp đồng là `src/main/resources/graphql/schema.graphqls` (SDL, contract-first giống Spring-WS/gRPC), không phải `@RequestMapping` theo URL. Client tự chọn field cần lấy trong query — khác REST luôn trả nguyên object cố định.

```
mvn compile exec:java -Dexec.mainClass=com.demo.securities.GraphQlMain -Dserver.port=8092
```
PowerShell: `mvn compile exec:java "-Dexec.mainClass=com.demo.securities.GraphQlMain" "-Dserver.port=8092"`

```
curl -X POST http://localhost:8092/graphql -H "Content-Type: application/json" \
  -d '{"query":"mutation { moTaiKhoan(khachHangId: \"KH0001\", loaiTaiKhoan: \"CO_SO\", soDuBanDau: 1000000) { soTaiKhoan trangThai soDuTien } }"}'

curl -X POST http://localhost:8092/graphql -H "Content-Type: application/json" \
  -d '{"query":"query { taiKhoan(so: \"TK000001\") { soTaiKhoan trangThai soDuTien } }"}'
```

**Idiom lỗi thứ 6, khác hẳn HTTP status (REST) hay SOAP Fault (JAX-WS/Spring-WS) hay `StatusRuntimeException` (gRPC)**: HTTP response GraphQL luôn là `200 OK`, lỗi nghiệp vụ nằm trong mảng `"errors"` cùng cấp `"data"` của response JSON, kèm `extensions.classification` (`BAD_REQUEST`/`NOT_FOUND`) — map qua `DataFetcherExceptionResolverAdapter` (`GraphQlExceptionResolver`).

### Gotcha đã gặp

**Auto-configuration của `spring-boot-starter-graphql` kích hoạt toàn cục theo dependency, không theo `scanBasePackages`.** Vì dependency này nằm chung `pom.xml` với toàn bộ project, khi chạy `SpringBootMain` (port 8087, vốn không liên quan gì tới GraphQL), log vẫn in ra "GraphQL schema inspection" và tự đăng ký `/graphql` trên port đó — do `schema.graphqls` nằm trên classpath chung và auto-configuration chỉ cần thấy dependency + resource là bật, không quan tâm `@SpringBootApplication(scanBasePackages=...)` giới hạn controller nào được quét. Vô hại (chỉ log warning "Unmapped fields" vì không có `@QueryMapping` nào trong scope đó), nhưng là hiệu ứng phụ đáng lưu ý khi nhiều entry point Spring Boot dùng chung 1 `pom.xml`.

## Chạy Quarkus (project riêng `quarkus-app/`, port 8094)

Nhánh "framework khác không Spring" — **project Maven hoàn toàn độc lập**, nằm cạnh project chính nhưng KHÔNG khai trong `<modules>`, không chung `pom.xml`/classpath/vòng đời build. Lý do tách: Quarkus tự làm "augmentation" lúc build (quét toàn bộ classpath để sinh bytecode tối ưu, build-time DI thay vì reflection runtime như Jersey/HK2) — nhét chung với Spring/CXF/Jersey/Hibernate hiện có sẽ vừa chậm vừa rủi ro quét nhầm.

`TaiKhoanResource` dùng `@Path`/`@GET`/`@POST` y hệt chuẩn JAX-RS đã quen ở `JaxRsMain` (`quarkus-rest` tương thích API JAX-RS), nhưng persistence qua **Panache** (`TaiKhoan.persist()`/`TaiKhoan.listAll()` ngay trên entity, không qua DAO/EntityManager riêng như Hibernate thuần ở `SpringFrameworkMain`). Vì là project/classpath riêng, không tái dùng được `KhachHangService` — validate tuổi khách hàng bằng 1 câu JDBC trực tiếp vào bảng `khach_hang`.

```
cd quarkus-app
mvn quarkus:dev
```
(Dev mode, hot reload khi sửa code — đặc trưng riêng của Quarkus, khác hẳn `mvn compile exec:java` của mọi entry point khác trong project. Đóng gói chạy production: `mvn package && java -jar target/quarkus-app/quarkus-run.jar`.)

```
curl -X POST http://localhost:8094/api/tai-khoan -H "Content-Type: application/json" \
  -d '{"khachHangId":"KH0001","loaiTaiKhoan":"CO_SO","soDuBanDau":1000000}'
curl http://localhost:8094/api/tai-khoan?khachHangId=KH0001
```

### Gotcha đã gặp

- **`quarkus-bom` nằm ở groupId `io.quarkus`, không phải `io.quarkus.platform`** — nhầm lẫn dễ gặp vì phần lớn tài liệu/archetype Quarkus mặc định dùng BOM tổng hợp `io.quarkus.platform:quarkus-bom` (bao gồm cả extension bên thứ 3); ở đây chỉ cần BOM lõi `io.quarkus:quarkus-bom` (chỉ extension chính thức) là đủ và tồn tại thật trên Maven Central, đã verify qua HTTP 200 trước khi dùng.
- **`quarkus-run.jar` không phải `-runner.jar`**: khác Quarkus 1.x/2.x cũ (đặt tên `<artifact>-runner.jar` phẳng trong `target/`), Quarkus 3.x đóng gói jar thin vào `target/quarkus-app/quarkus-run.jar` kèm thư mục `lib/`/`app/`/`quarkus/` riêng — chạy nhầm tên file cũ sẽ báo "Unable to access jarfile".

## Chức năng

- Quản lý khách hàng: thêm, sửa, xóa, tìm theo mã/tên, danh sách. Kiểm tra CCCD (12 số, không trùng), số điện thoại, email, ngày sinh.
- Quản lý tài khoản chứng khoán: mở tài khoản (yêu cầu khách hàng đủ 18 tuổi), khóa/mở khóa, đóng tài khoản (số dư phải bằng 0), nạp/rút tiền, xem danh sách theo khách hàng hoặc toàn bộ.
- Dữ liệu lưu trong PostgreSQL, bảng `khach_hang` và `tai_khoan_chung_khoan` trong schema cấu hình tại `db.properties`.
- Giới hạn đã biết: `IdGenerator` sinh mã theo kiểu `max + 1` bằng full-scan — an toàn khi dùng console (đơn luồng), nhưng có race condition nhẹ nếu nhiều request tạo mới đồng thời qua web service.

## So sánh các công nghệ đã build

| Công nghệ | Entry point | Ưu điểm | Nhược điểm | Khi nào dùng thực tế |
|---|---|---|---|---|
| **Java SE thuần** (`com.sun.net.httpserver`) | `WebMain` | Không thêm dependency nào; hiểu rõ từng byte request/response đi qua đâu; khởi động cực nhanh | Phải tự viết mọi thứ (routing, JSON, mã lỗi HTTP); không có hệ sinh thái middleware/plugin; không phù hợp app lớn | Tool nội bộ nhỏ, script, healthcheck endpoint, học cơ chế HTTP gốc |
| **Servlet thuần** | `ServletMain` | Chuẩn hóa (Servlet API), chạy được trên mọi container Jakarta EE; vẫn phải tự viết routing/JSON nên hiểu rõ cơ chế | Verbose — mỗi endpoint là 1 khối if/else theo `getPathInfo()`; không có validation/serialization tự động | Hệ thống cũ bắt buộc dùng WAR/servlet container, không được phép thêm framework |
| **JAX-RS (Jersey)** | `JaxRsMain` | Routing bằng annotation rõ ràng (`@Path`); JSON tự động (Jackson); `ExceptionMapper` tách lỗi gọn theo type | Cấu hình DI (HK2) hơi cồng kềnh cho việc nhỏ; thêm dependency Jersey + HK2 + Jackson | REST API chuẩn Jakarta EE, môi trường không dùng Spring nhưng muốn REST hiện đại |
| **JAX-WS (Apache CXF)** | `SoapMain` | Chuẩn hóa nghiêm ngặt qua WSDL; SOAP Fault có cấu trúc rõ; CXF hỗ trợ WS-* mạnh (Security, Addressing) | Nặng nề hơn REST nhiều lần (XML overhead, đặc tả phức tạp); JAX-WS bị gỡ khỏi JDK từ Java 11 nên luôn cần thêm dependency ngoài | Tích hợp hệ thống ngân hàng/doanh nghiệp cũ, đối tác yêu cầu hợp đồng WSDL nghiêm ngặt |
| **Spring MVC** (không Boot) | `SpringMvcMain` | `@RestController`/`@RestControllerAdvice` cực gọn so với tự viết Router; hệ sinh thái Spring đầy đủ (validation, AOP...) | Vẫn phải tự tay dựng Tomcat + `DispatcherServlet` + `ApplicationContext` — dễ sai (đã gặp 3 lỗi cấu hình khi build) | Học/hiểu cơ chế Spring MVC gốc trước khi dùng Boot; môi trường không được phép dùng Spring Boot |
| **Spring Boot** | `SpringBootMain` | Auto-configuration lo hết hạ tầng (embedded server, Jackson, MVC...); `@SpringBootApplication` + vài dòng là chạy; hệ sinh thái starter khổng lồ | "Magic" nhiều — khó biết chuyện gì đang xảy ra phía sau nếu không hiểu Spring MVC gốc; artifact/jar nặng hơn | Mặc định thực tế cho hầu hết dự án Java doanh nghiệp hiện nay — tốc độ phát triển là ưu tiên số 1 |
| **Spring WebFlux** | `SpringWebFluxMain` | Non-blocking, chịu tải cao với ít thread hơn hẳn; cùng annotation quen thuộc (`@RestController`) | Debug khó hơn (stack trace qua nhiều lớp reactive); nếu DAO vẫn blocking (như demo này) thì lợi ích thực tế bị giới hạn — phải dùng R2DBC mới "reactive thật" | Hệ thống I/O-bound cần xử lý rất nhiều kết nối đồng thời (gateway, streaming, high-throughput API) |
| **Spring Web Services** | `SpringWsMain` | Đúng triết lý contract-first (XSD trước, code sau) — hợp đồng rõ ràng, khó lệch; dispatch theo nội dung XML linh hoạt | Cấu hình thủ công nhiều hơn JAX-WS/CXF cho việc publish/deploy; hệ sinh thái/tài liệu ít phổ biến hơn CXF | Khi bắt buộc contract-first nghiêm ngặt và đã dùng Spring sẵn trong hệ thống |
| **Spring Framework thuần đầy đủ** (IoC+MVC+TX+AOP+Security+Hibernate) | `SpringFrameworkMain` | Thấy rõ TỪNG mảnh Spring hoạt động ra sao (không có "magic" nào giấu đi) — hiểu sâu trước khi dùng Boot; kiểm soát tuyệt đối từng bean | Rất nhiều code hạ tầng (Tomcat, DispatcherServlet, JPA, Security filter đều tay); dễ dính lỗi thứ tự khởi tạo/classloader tinh vi (đã gặp 3 lỗi khi build) — gần như không ai làm thế này trong dự án thật | Chỉ để HỌC — hiểu Spring Boot đang tự động hóa những gì trước khi tin tưởng giao phó cho nó |
| **gRPC** | `GrpcMain` | Nhị phân (protobuf) nhanh/nhẹ hơn JSON/XML nhiều; hợp đồng `.proto` chặt chẽ, sinh code đa ngôn ngữ; hỗ trợ streaming (unary/server/client/bidi) mà REST không có sẵn | Không đọc được bằng mắt thường (binary) — khó debug bằng `curl`; HTTP/2 gây khó khi qua proxy/load balancer cũ; hệ sinh thái client ít phổ biến hơn REST ở phía browser | Giao tiếp service-to-service nội bộ (microservices), cần hiệu năng cao, đã kiểm soát được cả 2 đầu client/server |
| **WebSocket** | `WebSocketMain` | Duy nhất trong project có kết nối 2 chiều thật (server tự đẩy dữ liệu không cần client hỏi); overhead thấp sau khi handshake xong (không lặp lại HTTP header mỗi lần) | Không có khái niệm request/response rõ ràng như REST — phải tự thiết kế "giao thức" message trên tầng ứng dụng; scale ngang khó hơn (kết nối stateful, cần sticky session hoặc pub/sub trung gian) | Real-time thật sự: chat, dashboard live-update, trading ticker, notification đẩy từ server |
| **GraphQL** | `GraphQlMain` | Client tự chọn field cần lấy — tránh over-fetching/under-fetching REST hay gặp; 1 request lấy được nhiều loại dữ liệu liên quan; schema tự mô tả (introspection) | Luôn trả `200 OK` kể cả khi lỗi — dễ bỏ sót lỗi nếu client không kiểm tra `errors[]`; cache HTTP thông thường (theo URL) không áp dụng được vì chỉ có 1 endpoint; N+1 query dễ xảy ra nếu không tối ưu resolver | Frontend/mobile cần linh hoạt field theo màn hình, nhiều client khác nhau (web/app) dùng chung 1 backend |
| **Quarkus** | `quarkus-app/` (project riêng, port 8094) | Build-time DI/augmentation — khởi động cực nhanh, footprint bộ nhớ thấp (mạnh cho container/serverless); dev mode hot-reload; Panache rút gọn code persistence đáng kể so với JPA/Hibernate thuần | Hệ sinh thái/tài liệu nhỏ hơn Spring nhiều; augmentation lúc build làm build time chậm hơn, đôi khi khó debug nếu extension không tương thích nhau; phải tách project riêng nếu dùng chung classpath với hệ thống cũ (như đã làm ở đây) | Microservices/container hóa cần khởi động nhanh, ít RAM (Kubernetes, serverless/FaaS), team chấp nhận đổi hệ sinh thái khỏi Spring |

## Đã phủ đủ 5/5 nhánh trong lộ trình gốc

Từ Java SE thuần → Jakarta EE (Servlet/JAX-RS/JAX-WS) → Spring ecosystem (MVC/Boot/WebFlux/WS) → mini Spring Framework (hiểu bản chất) → giao thức khác (gRPC/WebSocket/GraphQL) → framework khác không Spring (Quarkus): 14 entry point, 14 cách khác nhau để phục vụ cùng 1 nghiệp vụ quản lý tài khoản chứng khoán, cùng 1 database thật — đủ để so sánh trực tiếp ưu/nhược từng công nghệ thay vì chỉ đọc lý thuyết.
