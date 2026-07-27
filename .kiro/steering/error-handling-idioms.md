---
inclusion: auto
---

# Error Handling — 8 Idiom khác nhau trong 1 project

## Tổng quan

Project demo 8 cách báo lỗi hoàn toàn khác nhau, tùy công nghệ:

| # | Công nghệ | Cách báo lỗi | Ví dụ |
|---|-----------|--------------|-------|
| 1 | REST (WebMain) | HTTP status + JSON `{"error":"..."}` tay | `HttpUtil.sendJson(exchange, 404, Map.of("error", msg))` |
| 2 | Servlet | HTTP status + JSON tay (tái dùng pattern WebMain) | `resp.sendError()` hoặc `ServletHttpUtil.sendJson()` |
| 3 | JAX-RS | ExceptionMapper → HTTP status + JSON tự động | `NotFoundExceptionMapper` → 404 |
| 4 | Spring MVC/Boot | `@RestControllerAdvice` + `@ExceptionHandler` → HTTP status | `GlobalExceptionHandler` |
| 5 | SOAP CXF | `@WebFault` exception → `<soap:Fault><detail>` | `TaiKhoanFaultException` |
| 6 | Spring-WS | `@SoapFault` annotation trên exception | `TaiKhoanFaultException` (package springws) |
| 7 | gRPC | `Status` + `StatusRuntimeException` | `Status.NOT_FOUND.withDescription(msg)` |
| 8 | GraphQL | HTTP luôn 200, lỗi trong `"errors"[]` + `extensions.classification` | `DataFetcherExceptionResolverAdapter` |
| — | WebSocket | JSON message có field `error` (không có status code) | `{"error": "Khong tim thay..."}` |
| — | Optimistic Lock | 409 Conflict (thêm vào REST idiom) | `ObjectOptimisticLockingFailureException` → 409 |

## Chi tiết từng idiom

### 1. REST tay (WebMain) — Router.invoke()
```java
try { handler.handle(exchange, pathParams); }
catch (NotFoundException e)    → 404 + {"error": msg}
catch (ValidationException|DuplicateException|JsonParseException|...) → 400
catch (Exception e)            → 500 + {"error": "Lỗi hệ thống: " + msg}
```

### 2. Servlet — BaseApiServlet.handle()
Pattern tương tự WebMain: try/catch tập trung, map exception → status code + JSON.

### 3. JAX-RS — ExceptionMapper
```java
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    public Response toResponse(NotFoundException e) {
        return Response.status(404).entity(Map.of("error", e.getMessage())).build();
    }
}
```
Mỗi loại exception 1 class mapper nhỏ, JAX-RS runtime tự chọn mapper khớp nhất theo type.

### 4. Spring — @RestControllerAdvice
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,String> handleNotFound(NotFoundException e) { ... }
    
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    public Map<String,String> handleOptimisticLock(...) { ... }
}
```

### 5. SOAP CXF — @WebFault
```java
@WebFault(name = "TaiKhoanFault")
public class TaiKhoanFaultException extends Exception {
    private final TaiKhoanFaultInfo faultInfo;  // → <detail><TaiKhoanFault><message>...
}
// Trong impl: catch business exception → throw new TaiKhoanFaultException(msg, faultInfo)
```
Không có HTTP status trong SOAP — luôn HTTP 200 (hoặc 500 cho server error), lỗi nằm trong XML body.

### 6. Spring-WS — @SoapFault
```java
@SoapFault(faultCode = FaultCode.CLIENT)
public class TaiKhoanFaultException extends RuntimeException { ... }
```
Spring-WS tự bắt exception đánh dấu `@SoapFault` và generate `<soap:Fault>` response.

### 7. gRPC — Status
```java
catch (NotFoundException e) →
    responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
catch (ValidationException e) →
    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
catch (RuntimeException e) →
    responseObserver.onError(Status.INTERNAL.withDescription("Loi he thong").asRuntimeException());
```
gRPC có bộ status code riêng (NOT_FOUND, INVALID_ARGUMENT, INTERNAL, CANCELLED...) — không phải HTTP status.

### 8. GraphQL — errors array
```java
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof NotFoundException) → ErrorClassification BAD_REQUEST / NOT_FOUND
        // HTTP response vẫn là 200 OK, lỗi nằm trong:
        // {"data": null, "errors": [{"message": "...", "extensions": {"classification": "NOT_FOUND"}}]}
    }
}
```

## Tổng kết — Khi nào dùng gì

- **REST/HTTP status**: Standard, dễ hiểu, tooling support tốt (Postman, curl, browser)
- **SOAP Fault**: Khi đối tác yêu cầu WSDL, hệ thống cũ, cần structured fault detail
- **gRPC Status**: Service-to-service internal, performance-critical
- **GraphQL errors**: Frontend/mobile cần linh hoạt, partial success (1 field lỗi, field khác vẫn trả)
- **WebSocket message**: Real-time, không có request/response cycle rõ ràng
