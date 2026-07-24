package com.demo.securities.util;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern CCCD_PATTERN = Pattern.compile("\\d{12}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("0\\d{9}");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private Validator() { }

    public static boolean isValidCCCD(String cccd) {
        return cccd != null && CCCD_PATTERN.matcher(cccd).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static int tinhTuoi(LocalDate ngaySinh) {
        return Period.between(ngaySinh, LocalDate.now()).getYears();
    }

    public static boolean isAdult(LocalDate ngaySinh) {
        return ngaySinh != null && tinhTuoi(ngaySinh) >= 18;
    }
}
