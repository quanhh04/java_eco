---
inclusion: auto
---

# Spring Framework Deep Dive

## Spring trong project — 5 entry point, 5 mức độ

| Entry Point | Mức độ Spring | Điểm học |
|-------------|---------------|----------|
| SpringMvcMain (8086) | IoC + MVC tay | Hiểu DispatcherServlet, ApplicationContext |
| SpringBootMain (8087) | Boot wrapper | Thấy Boot đỡ bao nhiêu code |
| SpringWebFluxMain (8088) | Reactive | Mono/Flux, non-blocking, Netty |
| SpringWsMain (8089) | Spring-WS SOAP | Contract-first, @PayloadRoot |
| SpringFrameworkMain (8090) | ĐẦY ĐỦ (IoC+MVC+TX+AOP+Security+Hibernate) | Hiểu TẤT CẢ trước khi dùng Boot |

## IoC / Dependency Injection

### AppConfig (@Configuration, dùng chung)
```java
@Bean KhachHangRepository → new KhachHangRepositoryImpl()
@Bean TaiKhoanRepository → new TaiKhoanRepositoryImpl()
@Bean KhachHangService(repo, repo) → constructor injection
@Bean TaiKhoanService(repo, service) → constructor injection
```

### SpringMvcMain — tự dựng context
```java
AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
context.register(AppConfig.class, WebMvcConfig.class, Controller.class, ...);
// Tự tạo Tomcat, addServlet(DispatcherServlet(context))
```

### SpringBootMain — Boot lo hết
```java
@SpringBootApplication(scanBasePackages = "com.demo.securities.spring")
// Chỉ cần new SpringApplicationBuilder(...).run(args)
```

## DispatcherServlet / MVC

- `@RestController` = `@Controller` + `@ResponseBody` trên mọi method
- `@RequestMapping` / `@GetMapping` / `@PostMapping` — routing
- `@PathVariable`, `@RequestParam`, `@RequestBody` — binding
- `@RestControllerAdvice` + `@ExceptionHandler` — xử lý lỗi tập trung (`GlobalExceptionHandler`)
- Jackson serialize/deserialize JSON tự động (cần `-parameters` compiler flag cho @RequestParam không tên)

## Transaction Management (SpringFrameworkMain)

### Setup thủ công (JpaConfig)
```java
@EnableTransactionManagement
DataSource → DriverManagerDataSource (đọc db.properties)
EntityManagerFactory → LocalContainerEntityManagerFactoryBean + HibernatePersistenceProvider
TransactionManager → JpaTransactionManager(entityManagerFactory)
PersistenceAnnotationBeanPostProcessor → cho @PersistenceContext hoạt động
```

### Sử dụng
```java
@Transactional
public TaiKhoanEntity chuyenTien(String soNguon, String soDich, double soTien) {
    TaiKhoanEntity nguon = timTheoSo(soNguon);
    nguon.setSoDuTien(nguon.getSoDuTien() - soTien);  // dirty-check, chưa flush
    TaiKhoanEntity dich = timTheoSo(soDich);           // nếu exception → ROLLBACK cả phần trên
    dich.setSoDuTien(dich.getSoDuTien() + soTien);
    return nguon;  // commit → Hibernate flush cả 2 update cùng lúc
}
```

Key: entity đã managed trong persistence context → chỉ cần set property, Hibernate tự dirty-check và flush lúc commit. Không cần gọi save/merge tường minh.

## AOP (Aspect-Oriented Programming)

### Setup
- `AopConfig` với `@EnableAspectJAutoProxy`
- Dependency `aspectjweaver`

### LoggingAspect
```java
@Aspect
@Around("execution(* com.demo.securities.springfw.service.*.*(..))")
public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    try { return joinPoint.proceed(); }
    finally { System.out.println("[AOP] " + joinPoint.getSignature() + " mat " + (now-start) + "ms"); }
}
```

Service code KHÔNG biết gì về logging — AOP "dán" vào từ bên ngoài qua proxy. Spring tạo CGLIB proxy bọc TaiKhoanFwService, mọi call đi qua proxy trước.

## Security (Spring Security)

### Setup thủ công (SecurityConfig + DelegatingFilterProxy)
```java
@EnableWebSecurity
SecurityFilterChain:
  - csrf().disable()
  - GET /api/tai-khoan/** → permitAll()
  - mọi request khác → authenticated()
  - httpBasic()
UserDetailsService: InMemoryUserDetailsManager (admin/admin123, BCrypt)
```

### Đăng ký filter tay (SpringFrameworkMain)
```java
FilterDef filterDef = new FilterDef();
filterDef.setFilterName("springSecurityFilterChain");
filterDef.setFilter(new DelegatingFilterProxy("springSecurityFilterChain", context));
ctx.addFilterDef(filterDef);
// + FilterMap cho URL pattern "/*"
```

Boot tự đăng ký filter chain — ở đây phải tay vì dùng `addContext()` không có web.xml/ServletContainerInitializer.

## Optimistic Locking

### Entity
```java
@Entity @Table(name = "tai_khoan_chung_khoan")
public class TaiKhoanEntity {
    @Version private long version;  // Hibernate tự quản lý
}
```

### Cơ chế
- UPDATE thêm `AND version = ?`, tăng version +1
- Nếu 0 row affected → `OptimisticLockException`
- `GlobalExceptionHandler` bắt `ObjectOptimisticLockingFailureException` → 409 Conflict

### Demo tất định (OptimisticLockDemoService)
- Phiên A: đọc entity (version=1), giữ trong bộ nhớ
- Phiên B (EntityManager riêng): đọc cùng entity, sửa, commit → version DB = 2
- Phiên A: sửa trên entity cũ (version=1) → lúc commit, Hibernate thấy version lệch → exception
- Kết quả: 409 Conflict, số dư chỉ thay đổi 1 lần (từ phiên B), không lost update

## WebFlux — Reactive wrapper

```java
@GetMapping("/{so}")
public Mono<TaiKhoanDto> getBySo(@PathVariable String so) {
    return Mono.fromCallable(() -> taiKhoanService.timTheoSo(so))
               .subscribeOn(Schedulers.boundedElastic())  // offload blocking JDBC sang elastic pool
               .map(this::toDto);
}
```

Pattern "reactive facade trên blocking DAO cũ" — không đổi sang R2DBC, đúng thực tế khi team thêm WebFlux trên legacy code.

## Bảng ánh xạ: thủ công ↔ Boot tự động

| Mảnh | Thủ công (SpringFrameworkMain) | Boot tự động |
|------|-------------------------------|--------------|
| Server | Tự viết Tomcat, addContext, addServlet (~15 dòng) | 0 dòng |
| DispatcherServlet | Tự new + đăng ký servlet mapping | Auto-config |
| IoC | Tự new context, register từng class | @ComponentScan |
| Hibernate/JPA | JpaConfig ~40 dòng (DataSource, EMF, TxManager) | `spring.datasource.*` properties |
| Transaction | JpaTransactionManager bean + @EnableTransactionManagement | Tự bật |
| AOP | AopConfig + @EnableAspectJAutoProxy + dependency | Tự bật |
| Security | SecurityConfig + đăng ký DelegatingFilterProxy tay (~15 dòng) | `spring-boot-starter-security` |
| Version management | Tự tìm/ghim version (đã gặp 2 lỗi conflict) | BOM quản lý |
