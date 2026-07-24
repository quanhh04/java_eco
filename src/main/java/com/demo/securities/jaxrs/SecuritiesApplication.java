package com.demo.securities.jaxrs;

import com.demo.securities.service.KhachHangService;
import com.demo.securities.service.TaiKhoanService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class SecuritiesApplication extends ResourceConfig {

    public SecuritiesApplication(KhachHangService khachHangService, TaiKhoanService taiKhoanService) {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(khachHangService).to(KhachHangService.class);
                bind(taiKhoanService).to(TaiKhoanService.class);
            }
        });
        register(JacksonFeature.class);
        register(PingResource.class);
        register(KhachHangResource.class);
        register(TaiKhoanResource.class);
        register(ValidationExceptionMapper.class);
        register(DuplicateExceptionMapper.class);
        register(NotFoundExceptionMapper.class);
        register(IllegalArgumentExceptionMapper.class);
        register(NullPointerExceptionMapper.class);
        register(DateTimeParseExceptionMapper.class);
        register(ClassCastExceptionMapper.class);
        register(GenericExceptionMapper.class);
    }
}
