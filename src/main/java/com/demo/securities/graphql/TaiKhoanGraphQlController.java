package com.demo.securities.graphql;

import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.spring.dto.TaiKhoanDto;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * Nhánh "Giao thức khác - GraphQL": 1 endpoint HTTP POST duy nhất (/graphql),
 * client tự chọn field cần lấy (khác REST trả nguyên object cố định) - idiom
 * lỗi thứ 6 trong project: lỗi nằm trong mảng "errors" của response JSON,
 * KHÔNG dùng HTTP status như REST/gRPC/SOAP đã làm.
 *
 * @Controller (không phải @RestController) vì đây không map trực tiếp HTTP
 * request/response - Spring GraphQL tự lo phần đó, method chỉ cần trả về
 * đúng kiểu dữ liệu khớp schema.graphqls.
 */
@Controller
public class TaiKhoanGraphQlController {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanGraphQlController(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @QueryMapping
    public TaiKhoanDto taiKhoan(@Argument String so) {
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @QueryMapping
    public List<TaiKhoanDto> danhSachTaiKhoan(@Argument String khachHangId) {
        List<TaiKhoanChungKhoan> result = (khachHangId == null || khachHangId.isBlank())
                ? taiKhoanService.danhSachTatCa()
                : taiKhoanService.danhSachTheoKhachHang(khachHangId);
        return result.stream().map(TaiKhoanGraphQlController::toDto).toList();
    }

    @MutationMapping
    public TaiKhoanDto moTaiKhoan(@Argument String khachHangId, @Argument String loaiTaiKhoan,
                                   @Argument Double soDuBanDau) {
        double soDu = soDuBanDau == null ? 0 : soDuBanDau;
        return toDto(taiKhoanService.moTaiKhoan(khachHangId, LoaiTaiKhoan.valueOf(loaiTaiKhoan.toUpperCase()), soDu));
    }

    @MutationMapping
    public TaiKhoanDto napTien(@Argument String so, @Argument double soTien) {
        taiKhoanService.napTien(so, soTien);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @MutationMapping
    public TaiKhoanDto rutTien(@Argument String so, @Argument double soTien) {
        taiKhoanService.rutTien(so, soTien);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @MutationMapping
    public TaiKhoanDto khoaTaiKhoan(@Argument String so) {
        taiKhoanService.khoaTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @MutationMapping
    public TaiKhoanDto moKhoaTaiKhoan(@Argument String so) {
        taiKhoanService.moKhoaTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    @MutationMapping
    public TaiKhoanDto dongTaiKhoan(@Argument String so) {
        taiKhoanService.dongTaiKhoan(so);
        return toDto(taiKhoanService.timTheoSo(so));
    }

    private static TaiKhoanDto toDto(TaiKhoanChungKhoan taiKhoan) {
        return new TaiKhoanDto(
                taiKhoan.getSoTaiKhoan(),
                taiKhoan.getKhachHangId(),
                taiKhoan.getLoaiTaiKhoan().name(),
                taiKhoan.getTrangThai().name(),
                taiKhoan.getNgayMo().toString(),
                taiKhoan.getSoDuTien());
    }
}
