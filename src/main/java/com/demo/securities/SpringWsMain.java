package com.demo.securities;

import com.demo.securities.spring.AppConfig;
import com.demo.securities.springws.TaiKhoanEndpoint;
import com.demo.securities.springws.WsConfig;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Nhánh "Spring ecosystem - Spring Web Services (SOAP, contract-first)": tự dựng
 * Tomcat nhúng + MessageDispatcherServlet bằng tay (giống SpringMvcMain), route
 * dựa theo @PayloadRoot thay vì URL. Contract-first thật: XSD viết trước
 * (src/main/resources/tai-khoan-ws.xsd), WSDL sinh động lúc runtime từ XSD đó
 * qua DefaultWsdl11Definition (không hand-write WSDL).
 */
public class SpringWsMain {

    public static void main(String[] args) throws Exception {
        int port = Integer.getInteger("server.port", 8089);

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class, WsConfig.class, TaiKhoanEndpoint.class);
        context.refresh();

        Path baseDir = Files.createTempDirectory("springws-tomcat");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", baseDir.toAbsolutePath().toString());
        ctx.setParentClassLoader(SpringWsMain.class.getClassLoader());

        MessageDispatcherServlet servlet = new MessageDispatcherServlet(context);
        servlet.setTransformWsdlLocations(true);
        // loadOnStartup: ep servlet init() chay tren main thread luc tomcat.start(),
        // khong de lazy-init tren worker thread khi co request dau tien - vi vay
        // moi tranh duoc ClassNotFoundException do reflection lookup (default
        // strategies cua Spring-WS) nhay cam voi thread context classloader.
        Tomcat.addServlet(ctx, "messageDispatcherServlet", servlet).setLoadOnStartup(1);
        ctx.addServletMappingDecoded("/tai-khoan-ws/*", "messageDispatcherServlet");

        tomcat.start();
        System.out.println("Spring-WS (SOAP contract-first) dang chay tai http://localhost:" + port + "/tai-khoan-ws");
        System.out.println("WSDL: http://localhost:" + port + "/tai-khoan-ws/tai-khoan-ws.wsdl");
        tomcat.getServer().await();
    }
}
