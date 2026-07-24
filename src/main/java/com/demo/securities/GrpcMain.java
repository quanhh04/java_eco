package com.demo.securities;

import com.demo.securities.grpc.TaiKhoanGrpcServiceImpl;
import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.repository.impl.KhachHangRepositoryImpl;
import com.demo.securities.repository.impl.TaiKhoanRepositoryImpl;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Nhanh "Giao thuc khac - gRPC": khac han REST/SOAP da lam - HTTP/2 nhi phan
 * (protobuf), hop dong .proto (contract-first giong Spring-WS nhung binary
 * thay vi XML), co server-streaming (TheoDoiSoDu) ma REST khong lam duoc.
 */
public class GrpcMain {

    public static void main(String[] args) throws Exception {
        KhachHangRepository khachHangRepository = new KhachHangRepositoryImpl();
        TaiKhoanRepository taiKhoanRepository = new TaiKhoanRepositoryImpl();

        KhachHangService khachHangService = new KhachHangService(khachHangRepository, taiKhoanRepository);
        TaiKhoanService taiKhoanService = new TaiKhoanService(taiKhoanRepository, khachHangService);

        int port = Integer.getInteger("server.port", 8093);

        Server server = ServerBuilder.forPort(port)
                .addService(new TaiKhoanGrpcServiceImpl(taiKhoanService))
                .build()
                .start();

        System.out.println("gRPC server (TaiKhoanGrpcService) dang chay tai localhost:" + port);
        server.awaitTermination();
    }
}
