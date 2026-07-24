package com.demo.securities;

import com.demo.securities.repository.KhachHangRepository;
import com.demo.securities.repository.TaiKhoanRepository;
import com.demo.securities.repository.impl.KhachHangRepositoryImpl;
import com.demo.securities.repository.impl.TaiKhoanRepositoryImpl;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import com.demo.securities.ui.ConsoleMenu;

public class Main {

    public static void main(String[] args) {
        KhachHangRepository khachHangRepository = new KhachHangRepositoryImpl();
        TaiKhoanRepository taiKhoanRepository = new TaiKhoanRepositoryImpl();

        KhachHangService khachHangService = new KhachHangService(khachHangRepository, taiKhoanRepository);
        TaiKhoanService taiKhoanService = new TaiKhoanService(taiKhoanRepository, khachHangService);

        new ConsoleMenu(khachHangService, taiKhoanService).run();
    }
}
