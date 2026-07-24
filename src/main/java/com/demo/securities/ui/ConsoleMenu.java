package com.demo.securities.ui;

import com.demo.securities.exception.DuplicateException;
import com.demo.securities.exception.NotFoundException;
import com.demo.securities.exception.ValidationException;
import com.demo.securities.model.GioiTinh;
import com.demo.securities.model.KhachHang;
import com.demo.securities.model.LoaiTaiKhoan;
import com.demo.securities.model.TaiKhoanChungKhoan;
import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Scanner scanner = new Scanner(System.in);
    private final KhachHangService khachHangService;
    private final TaiKhoanService taiKhoanService;

    public ConsoleMenu(KhachHangService khachHangService, TaiKhoanService taiKhoanService) {
        this.khachHangService = khachHangService;
        this.taiKhoanService = taiKhoanService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("===== QUAN LY KHACH HANG MO TAI KHOAN CHUNG KHOAN =====");
            System.out.println("1. Quan ly khach hang");
            System.out.println("2. Quan ly tai khoan chung khoan");
            System.out.println("0. Thoat");
            switch (readInt("Chon: ")) {
                case 1 -> menuKhachHang();
                case 2 -> menuTaiKhoan();
                case 0 -> running = false;
                default -> System.out.println("Lua chon khong hop le");
            }
        }
        System.out.println("Tam biet!");
    }

    // ---------- Menu Khach hang ----------

    private void menuKhachHang() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- QUAN LY KHACH HANG ---");
            System.out.println("1. Them khach hang");
            System.out.println("2. Sua khach hang");
            System.out.println("3. Xoa khach hang");
            System.out.println("4. Tim theo ma");
            System.out.println("5. Tim theo ten");
            System.out.println("6. Danh sach khach hang");
            System.out.println("0. Quay lai");
            int choice = readInt("Chon: ");
            try {
                switch (choice) {
                    case 1 -> themKhachHang();
                    case 2 -> suaKhachHang();
                    case 3 -> xoaKhachHang();
                    case 4 -> timKhachHangTheoMa();
                    case 5 -> timKhachHangTheoTen();
                    case 6 -> inDanhSachKhachHang(khachHangService.danhSach());
                    case 0 -> back = true;
                    default -> System.out.println("Lua chon khong hop le");
                }
            } catch (ValidationException | NotFoundException | DuplicateException e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }
    }

    private void themKhachHang() {
        String hoTen = readString("Ho ten: ");
        LocalDate ngaySinh = readDate("Ngay sinh (dd/MM/yyyy): ");
        GioiTinh gioiTinh = readGioiTinh();
        String cccd = readString("So CCCD (12 so): ");
        String phone = readString("So dien thoai: ");
        String email = readString("Email: ");
        String diaChi = readString("Dia chi: ");
        KhachHang kh = khachHangService.themKhachHang(hoTen, ngaySinh, gioiTinh, cccd, phone, email, diaChi);
        System.out.println("Da them khach hang, ma: " + kh.getId());
    }

    private void suaKhachHang() {
        String id = readString("Ma khach hang can sua: ");
        String hoTen = readString("Ho ten moi: ");
        LocalDate ngaySinh = readDate("Ngay sinh moi (dd/MM/yyyy): ");
        GioiTinh gioiTinh = readGioiTinh();
        String phone = readString("So dien thoai moi: ");
        String email = readString("Email moi: ");
        String diaChi = readString("Dia chi moi: ");
        khachHangService.suaKhachHang(id, hoTen, ngaySinh, gioiTinh, phone, email, diaChi);
        System.out.println("Da cap nhat khach hang " + id);
    }

    private void xoaKhachHang() {
        String id = readString("Ma khach hang can xoa: ");
        khachHangService.xoaKhachHang(id);
        System.out.println("Da xoa khach hang " + id);
    }

    private void timKhachHangTheoMa() {
        String id = readString("Ma khach hang: ");
        System.out.println(khachHangService.timTheoId(id));
    }

    private void timKhachHangTheoTen() {
        String keyword = readString("Nhap ten can tim: ");
        inDanhSachKhachHang(khachHangService.timTheoTen(keyword));
    }

    private void inDanhSachKhachHang(List<KhachHang> list) {
        if (list.isEmpty()) {
            System.out.println("Khong co du lieu");
            return;
        }
        for (KhachHang kh : list) {
            System.out.println(kh);
        }
    }

    // ---------- Menu Tai khoan ----------

    private void menuTaiKhoan() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- QUAN LY TAI KHOAN CHUNG KHOAN ---");
            System.out.println("1. Mo tai khoan moi");
            System.out.println("2. Khoa tai khoan");
            System.out.println("3. Mo khoa tai khoan");
            System.out.println("4. Dong tai khoan");
            System.out.println("5. Nap tien");
            System.out.println("6. Rut tien");
            System.out.println("7. Danh sach tai khoan theo khach hang");
            System.out.println("8. Danh sach tat ca tai khoan");
            System.out.println("0. Quay lai");
            int choice = readInt("Chon: ");
            try {
                switch (choice) {
                    case 1 -> moTaiKhoan();
                    case 2 -> khoaTaiKhoan();
                    case 3 -> moKhoaTaiKhoan();
                    case 4 -> dongTaiKhoan();
                    case 5 -> napTien();
                    case 6 -> rutTien();
                    case 7 -> danhSachTheoKhachHang();
                    case 8 -> inDanhSachTaiKhoan(taiKhoanService.danhSachTatCa());
                    case 0 -> back = true;
                    default -> System.out.println("Lua chon khong hop le");
                }
            } catch (ValidationException | NotFoundException e) {
                System.out.println("Loi: " + e.getMessage());
            }
        }
    }

    private void moTaiKhoan() {
        String khachHangId = readString("Ma khach hang: ");
        LoaiTaiKhoan loai = readLoaiTaiKhoan();
        double soDuBanDau = readDouble("So du ban dau: ");
        TaiKhoanChungKhoan tk = taiKhoanService.moTaiKhoan(khachHangId, loai, soDuBanDau);
        System.out.println("Da mo tai khoan, so tai khoan: " + tk.getSoTaiKhoan());
    }

    private void khoaTaiKhoan() {
        String so = readString("So tai khoan: ");
        taiKhoanService.khoaTaiKhoan(so);
        System.out.println("Da khoa tai khoan " + so);
    }

    private void moKhoaTaiKhoan() {
        String so = readString("So tai khoan: ");
        taiKhoanService.moKhoaTaiKhoan(so);
        System.out.println("Da mo khoa tai khoan " + so);
    }

    private void dongTaiKhoan() {
        String so = readString("So tai khoan: ");
        taiKhoanService.dongTaiKhoan(so);
        System.out.println("Da dong tai khoan " + so);
    }

    private void napTien() {
        String so = readString("So tai khoan: ");
        double soTien = readDouble("So tien nap: ");
        taiKhoanService.napTien(so, soTien);
        System.out.println("Nap tien thanh cong");
    }

    private void rutTien() {
        String so = readString("So tai khoan: ");
        double soTien = readDouble("So tien rut: ");
        taiKhoanService.rutTien(so, soTien);
        System.out.println("Rut tien thanh cong");
    }

    private void danhSachTheoKhachHang() {
        String khachHangId = readString("Ma khach hang: ");
        inDanhSachTaiKhoan(taiKhoanService.danhSachTheoKhachHang(khachHangId));
    }

    private void inDanhSachTaiKhoan(List<TaiKhoanChungKhoan> list) {
        if (list.isEmpty()) {
            System.out.println("Khong co du lieu");
            return;
        }
        for (TaiKhoanChungKhoan tk : list) {
            System.out.println(tk);
        }
    }

    // ---------- Input helpers ----------

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so nguyen hop le");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Vui long nhap so hop le");
            }
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return LocalDate.parse(line, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("Dinh dang ngay khong hop le, vi du: 25/12/2000");
            }
        }
    }

    private GioiTinh readGioiTinh() {
        while (true) {
            String line = readString("Gioi tinh (NAM/NU/KHAC): ").toUpperCase();
            try {
                return GioiTinh.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Gioi tinh khong hop le");
            }
        }
    }

    private LoaiTaiKhoan readLoaiTaiKhoan() {
        while (true) {
            String line = readString("Loai tai khoan (CO_SO/KY_QUY): ").toUpperCase();
            try {
                return LoaiTaiKhoan.valueOf(line);
            } catch (IllegalArgumentException e) {
                System.out.println("Loai tai khoan khong hop le");
            }
        }
    }
}
