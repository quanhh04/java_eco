package com.demo.securities.grpc;

import com.demo.securities.exception.DuplicateException;
import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.TaiKhoanService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

/**
 * Loi trong gRPC dung Status/StatusRuntimeException (khong phai exception thuong,
 * khong phai HTTP status, khong phai SOAP Fault) - idiom rieng cua gRPC.
 */
public class TaiKhoanGrpcServiceImpl extends TaiKhoanGrpcServiceGrpc.TaiKhoanGrpcServiceImplBase {

    private final TaiKhoanService taiKhoanService;

    public TaiKhoanGrpcServiceImpl(TaiKhoanService taiKhoanService) {
        this.taiKhoanService = taiKhoanService;
    }

    @Override
    public void moTaiKhoan(MoTaiKhoanRequest request, StreamObserver<TaiKhoanResponse> responseObserver) {
        wrap(responseObserver, () -> taiKhoanService.moTaiKhoan(
                request.getKhachHangId(),
                LoaiTaiKhoan.valueOf(request.getLoaiTaiKhoan().toUpperCase()),
                request.getSoDuBanDau()));
    }

    @Override
    public void truyVanTaiKhoan(TruyVanRequest request, StreamObserver<TaiKhoanResponse> responseObserver) {
        wrap(responseObserver, () -> taiKhoanService.timTheoSo(request.getSoTaiKhoan()));
    }

    @Override
    public void napTien(SoTienRequest request, StreamObserver<TaiKhoanResponse> responseObserver) {
        wrap(responseObserver, () -> {
            taiKhoanService.napTien(request.getSoTaiKhoan(), request.getSoTien());
            return taiKhoanService.timTheoSo(request.getSoTaiKhoan());
        });
    }

    @Override
    public void rutTien(SoTienRequest request, StreamObserver<TaiKhoanResponse> responseObserver) {
        wrap(responseObserver, () -> {
            taiKhoanService.rutTien(request.getSoTaiKhoan(), request.getSoTien());
            return taiKhoanService.timTheoSo(request.getSoTaiKhoan());
        });
    }

    @Override
    public void khoaTaiKhoan(TruyVanRequest request, StreamObserver<TaiKhoanResponse> responseObserver) {
        wrap(responseObserver, () -> {
            taiKhoanService.khoaTaiKhoan(request.getSoTaiKhoan());
            return taiKhoanService.timTheoSo(request.getSoTaiKhoan());
        });
    }

    @Override
    public void moKhoaTaiKhoan(TruyVanRequest request, StreamObserver<TaiKhoanResponse> responseObserver) {
        wrap(responseObserver, () -> {
            taiKhoanService.moKhoaTaiKhoan(request.getSoTaiKhoan());
            return taiKhoanService.timTheoSo(request.getSoTaiKhoan());
        });
    }

    @Override
    public void dongTaiKhoan(TruyVanRequest request, StreamObserver<TaiKhoanResponse> responseObserver) {
        wrap(responseObserver, () -> {
            taiKhoanService.dongTaiKhoan(request.getSoTaiKhoan());
            return taiKhoanService.timTheoSo(request.getSoTaiKhoan());
        });
    }

    @Override
    public void theoDoiSoDu(TruyVanRequest request, StreamObserver<TaiKhoanResponse> responseObserver) {
        try {
            String soTaiKhoan = request.getSoTaiKhoan();
            for (int i = 0; i < 5; i++) {
                TaiKhoanChungKhoan taiKhoan = taiKhoanService.timTheoSo(soTaiKhoan);
                responseObserver.onNext(toResponse(taiKhoan));
                Thread.sleep(1000);
            }
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            responseObserver.onError(Status.CANCELLED.withDescription("Stream bi ngat").asRuntimeException());
        }
    }

    private interface GrpcAction {
        TaiKhoanChungKhoan run();
    }

    private void wrap(StreamObserver<TaiKhoanResponse> responseObserver, GrpcAction action) {
        try {
            TaiKhoanChungKhoan taiKhoan = action.run();
            responseObserver.onNext(toResponse(taiKhoan));
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        } catch (ValidationException | DuplicateException | IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (RuntimeException e) {
            responseObserver.onError(Status.INTERNAL.withDescription("Loi he thong: " + e.getMessage()).asRuntimeException());
        }
    }

    private static TaiKhoanResponse toResponse(TaiKhoanChungKhoan taiKhoan) {
        return TaiKhoanResponse.newBuilder()
                .setSoTaiKhoan(taiKhoan.getSoTaiKhoan())
                .setKhachHangId(taiKhoan.getKhachHangId())
                .setLoaiTaiKhoan(taiKhoan.getLoaiTaiKhoan().name())
                .setTrangThai(taiKhoan.getTrangThai().name())
                .setNgayMo(taiKhoan.getNgayMo().toString())
                .setSoDuTien(taiKhoan.getSoDuTien())
                .build();
    }
}
