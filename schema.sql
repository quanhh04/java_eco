CREATE SCHEMA IF NOT EXISTS account_management;

CREATE TABLE IF NOT EXISTS account_management.khach_hang (
    id             VARCHAR(10)  PRIMARY KEY,
    ho_ten         VARCHAR(255) NOT NULL,
    ngay_sinh      DATE         NOT NULL,
    gioi_tinh      VARCHAR(10)  NOT NULL,
    so_cccd        VARCHAR(12)  NOT NULL UNIQUE,
    so_dien_thoai  VARCHAR(15)  NOT NULL,
    email          VARCHAR(255) NOT NULL,
    dia_chi        VARCHAR(500),
    ngay_tao       TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS account_management.tai_khoan_chung_khoan (
    so_tai_khoan   VARCHAR(10)    PRIMARY KEY,
    khach_hang_id  VARCHAR(10)    NOT NULL REFERENCES account_management.khach_hang(id),
    loai_tai_khoan VARCHAR(10)    NOT NULL,
    trang_thai     VARCHAR(10)    NOT NULL,
    ngay_mo        TIMESTAMP      NOT NULL,
    so_du_tien     NUMERIC(18,2)  NOT NULL DEFAULT 0
);
