---
inclusion: auto
---

# Java Fundamentals trong Project

## OOP & Design Patterns

### Model layer (POJO thuần)
- `KhachHang`, `TaiKhoanChungKhoan` — class thường, constructor đầy đủ, getter/setter, `toString()`
- Enum: `GioiTinh` (NAM/NU), `LoaiTaiKhoan` (CO_SO/MARGIN), `TrangThaiTaiKhoan` (HOAT_DONG/TAM_KHOA/DA_DONG)
- Không dùng Lombok/record cho model chính (giữ Java thuần để học)

### Repository Pattern
- Interface `KhachHangRepository` / `TaiKhoanRepository` — định nghĩa contract truy vấn
- Impl dùng JDBC thuần: `PreparedStatement`, `ResultSet`, try-with-resources
- Parameterized query chống SQL injection (`WHERE so_tai_khoan = ?`)
- Mỗi method mở/đóng Connection riêng (stateless, thread-safe)

### Service Layer
- `KhachHangService` / `TaiKhoanService` — nghiệp vụ + validation
- Constructor injection (DI thủ công trong Main.java, Spring @Bean trong AppConfig)
- Ném exception cụ thể theo loại lỗi (Validation/NotFound/Duplicate)

### Exception Hierarchy
```
RuntimeException
  ├── ValidationException  (dữ liệu không hợp lệ → 400)
  ├── NotFoundException    (không tìm thấy → 404)
  ├── DuplicateException   (trùng CCCD → 400)
  └── DataAccessException  (lỗi DB → 500)
```

## JDBC thuần

```java
// Pattern lặp lại trong TaiKhoanRepositoryImpl:
try (Connection conn = DatabaseConfig.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, value);
    try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) { result.add(map(rs)); }
    }
} catch (SQLException e) {
    throw new DataAccessException("msg", e);
}
```

Key points:
- `DatabaseConfig` — static utility, đọc `db.properties` 1 lần trong static block
- `Class.forName("org.postgresql.Driver")` — load driver (cần thiết cho một số setup cũ)
- Mỗi method tự getConnection() → không dùng connection pool (đơn giản hóa cho demo)
- `map(ResultSet)` — tự viết mapping từ row sang object

## Generics & Functional

`IdGenerator.nextId(List<T>, Function<T, String>, prefix, digits)` — generic method dùng method reference `KhachHang::getId` để sinh ID tiếp theo (max+1 pattern, có race condition nếu concurrent).

## Java 21 Features

- `Executors.newVirtualThreadPerTaskExecutor()` trong `WebServer` — mỗi request 1 virtual thread
- Record types dùng cho DTO: `TaiKhoanDto(String soTaiKhoan, ...)` trong các package jaxrs/spring

## Custom HTTP Framework (package `web/`)

Tự viết từ đầu KHÔNG dependency:
- `WebServer` — wrap `com.sun.net.httpserver.HttpServer`, gắn virtual thread executor
- `Router` — pattern matching URL segments, hỗ trợ `{param}`, dispatch theo HTTP method
- `JsonWriter` / `JsonParser` — tự viết serialize/deserialize JSON (không Jackson)
- `RequestBody` — helper parse body JSON thành `Map<String,Object>`
- `HttpUtil` — sendJson, parseQuery, readBody
- `RouteHandler` — functional interface `(HttpExchange, Map<String,String> pathParams) -> void`

Đây là nền tảng để hiểu framework thực sự làm gì: routing, serialization, error mapping.

## Dependency Injection — 3 mức

1. **Tay** (Main.java): `new KhachHangService(new KhachHangRepositoryImpl(), ...)`
2. **Spring @Bean** (AppConfig): khai báo tường minh, Spring inject qua method parameter
3. **Spring @ComponentScan** (SpringBootMain): tự quét annotation, auto-wire

## Validation (Validator utility)

- `isBlank(String)` — null/empty check
- `isValidCCCD(String)` — regex 12 chữ số
- `isValidPhone(String)` — 10 số, bắt đầu bằng 0
- `isValidEmail(String)` — regex cơ bản
- `isAdult(LocalDate)` — >= 18 tuổi (so với `LocalDate.now()`)

## Package OOP Demo (`com.demo.securities.oop`)

Package **độc lập** (không cần DB) minh họa FULL OOP Java qua nghiệp vụ "tính phí giao dịch chứng khoán".

Chạy: `mvn compile exec:java "-Dexec.mainClass=com.demo.securities.oop.OopDemo"`

### Các khái niệm OOP được minh họa

| Khái niệm | File | Chi tiết |
|-----------|------|----------|
| **Encapsulation** | `AbstractChinhSachPhi`, `GiaoDich`, `GiaoDichProcessor` | private field, getter/setter, validate constructor, Collections.unmodifiableList() |
| **Inheritance** | `ChinhSachPhiCoSo/KyQuy/Vip extends AbstractChinhSachPhi` | super(), override, kế thừa template method |
| **Polymorphism** | `ChinhSachPhi` interface + 3 impl, `LoaiGiaoDich` enum abstract method | Cùng `tinhPhi()` → 3 kết quả khác; runtime dynamic dispatch |
| **Abstraction** | `ChinhSachPhi` (interface), `AbstractChinhSachPhi` (abstract class) | contract vs partial impl, Template Method pattern |
| **Enum nâng cao** | `LoaiGiaoDich` | Field, constructor, abstract method per constant |
| **Record** | `GiaoDich` | Immutable value object, compact constructor validation |
| **Sealed class** | `SuKienGiaoDich` | permits, final subclass, exhaustive pattern matching switch |
| **Composition** | `ChinhSachPhiVip` has-a `ChinhSachPhi` | Has-a vs Is-a, đổi strategy runtime |
| **Functional interface** | `GiaoDichListener` | @FunctionalInterface, lambda, default method `andThen()` |
| **Comparator/Stream** | `ThongKeGiaoDich` | Comparator.comparing, lambda Predicate, method reference |
| **Anonymous class vs Lambda** | `OopDemo`, `ThongKeGiaoDich` | Cùng 1 logic, 2 cách viết |
| **Static vs Instance** | `GiaoDichProcessor` | static sinhMaGiaoDich() vs instance thucHienGiaoDich() |
| **final** | `AbstractChinhSachPhi.tinhPhi()` final, `GiaoDichThanhCong` final class | Chặn override / chặn kế thừa |
| **Inner class** | `SuKienGiaoDich.GiaoDichThanhCong` (static nested) | Gom logic liên quan, không cần outer instance |
| **Access modifier** | protected constructor, private field, package-private | Kiểm soát visibility |
| **this / super** | `ChinhSachPhiVip.soTienTietKiem()` dùng this, constructor dùng super() | Tham chiếu instance hiện tại vs cha |
| **Generic method** | `IdGenerator.nextId<T>()`, `Predicate<GiaoDich>` | Type parameter, method reference |

### Sơ đồ class hierarchy

```
                    ChinhSachPhi (interface)
                         |
              AbstractChinhSachPhi (abstract class)
              /          |              \
ChinhSachPhiCoSo  ChinhSachPhiKyQuy  ChinhSachPhiVip (has-a ChinhSachPhi)

         SuKienGiaoDich (sealed abstract)
         /           |              \
GiaoDichThanhCong  GiaoDichThatBai  GiaoDichChoXuLy  (all final)

LoaiGiaoDich (enum with abstract method)
  MUA / BAN / CHUYEN_KHOAN (each overrides tinhPhiCoSo)
```
