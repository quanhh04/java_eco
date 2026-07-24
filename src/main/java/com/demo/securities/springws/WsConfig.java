package com.demo.securities.springws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@Configuration
@EnableWs
public class WsConfig {

    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.demo.securities.springws");
        return marshaller;
    }

    /**
     * Khai bean tường minh (thay vì để MessageDispatcherServlet tự
     * ClassUtils.forName("...SaajSoapMessageFactory") theo cơ chế
     * "default strategies" đọc từ file properties) — Tomcat WebappClassLoader
     * trong context nhúng của mình không load được class đó qua reflection,
     * nên tự new() ra thẳng để né hẳn bước tra cứu đó.
     */
    @Bean
    public WebServiceMessageFactory messageFactory() {
        return new SaajSoapMessageFactory();
    }

    @Bean
    public XsdSchema taiKhoanSchema() {
        return new SimpleXsdSchema(new ClassPathResource("tai-khoan-ws.xsd"));
    }

    @Bean(name = "tai-khoan-ws")
    public DefaultWsdl11Definition wsdl11Definition(XsdSchema taiKhoanSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("TaiKhoanPort");
        wsdl11Definition.setLocationUri("/tai-khoan-ws");
        wsdl11Definition.setTargetNamespace(Namespaces.NS);
        wsdl11Definition.setSchema(taiKhoanSchema);
        return wsdl11Definition;
    }
}
