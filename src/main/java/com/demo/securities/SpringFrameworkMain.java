package com.demo.securities;

import com.demo.securities.spring.AppConfig;
import com.demo.securities.spring.GlobalExceptionHandler;
import com.demo.securities.springfw.aop.LoggingAspect;
import com.demo.securities.springfw.config.AopConfig;
import com.demo.securities.springfw.config.JpaConfig;
import com.demo.securities.springfw.config.SecurityConfig;
import com.demo.securities.springfw.dao.TaiKhoanDao;
import com.demo.securities.springfw.service.TaiKhoanFwService;
import com.demo.securities.springfw.web.TaiKhoanFwController;
import com.demo.securities.springmvc.WebMvcConfig;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Nhánh "Spring Framework thuần" — ghép đủ 5 mảnh nền tảng KHÔNG qua Spring Boot:
 * IoC/DI (AppConfig, TaiKhoanFwService...), DispatcherServlet/MVC (WebMvcConfig có
 * sẵn từ SpringMvcMain), Transaction Management (JpaConfig: JpaTransactionManager +
 * @Transactional), AOP (AopConfig + LoggingAspect), Security/Filter (SecurityConfig +
 * DelegatingFilterProxy đăng ký tay), và Hibernate setup thủ công (JpaConfig).
 *
 * Khác SpringMvcMain (chỉ IoC+MVC), entry point này là bản đầy đủ để thấy toàn bộ
 * Spring Framework hoạt động ra sao trước khi dùng Spring Boot.
 */
public class SpringFrameworkMain {

    public static void main(String[] args) throws Exception {
        int port = Integer.getInteger("server.port", 8090);

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(
                AppConfig.class,
                WebMvcConfig.class,
                JpaConfig.class,
                AopConfig.class,
                SecurityConfig.class,
                TaiKhoanDao.class,
                TaiKhoanFwService.class,
                TaiKhoanFwController.class,
                LoggingAspect.class,
                GlobalExceptionHandler.class);

        Path baseDir = Files.createTempDirectory("springfw-tomcat");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context ctx = tomcat.addContext("", baseDir.toAbsolutePath().toString());
        // Da hoc tu SpringWsMain: Tomcat WebappClassLoader mac dinh khong ke thua dung
        // classloader ung dung, gay ClassNotFoundException khi Spring tra cuu class qua
        // reflection (default strategies, JPA provider...). Set truoc de tranh lap lai.
        ctx.setParentClassLoader(SpringFrameworkMain.class.getClassLoader());

        // Tomcat khoi tao Filter TRUOC Servlet luc context start. Neu khong tu refresh()
        // o day, DelegatingFilterProxy se la ben dau tien dung toi context (truoc ca
        // DispatcherServlet), tu trigger refresh() nhung luc do chua co ServletContext
        // gan vao context -> @EnableWebMvc (resourceHandlerMapping) loi "No ServletContext
        // set". Tu gan ServletContext + refresh truoc, ca filter lan servlet sau do chi
        // thay context da active va dung lai, khong refresh lan nua.
        context.setServletContext(ctx.getServletContext());
        context.refresh();

        // Spring Security: dang ky DelegatingFilterProxy tay (khong co web.xml/
        // ServletContainerInitializer tu dong vi dung addContext, khong phai addWebapp).
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("springSecurityFilterChain");
        filterDef.setFilter(new DelegatingFilterProxy("springSecurityFilterChain", context));
        ctx.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("springSecurityFilterChain");
        filterMap.addURLPattern("/*");
        ctx.addFilterMap(filterMap);

        // loadOnStartup=1: ep DispatcherServlet.init() (va toan bo context.refresh(),
        // bao gom Hibernate/JPA bootstrap) chay luc tomcat.start() thay vi lazy tren
        // request dau tien.
        Tomcat.addServlet(ctx, "dispatcherServlet", new DispatcherServlet(context)).setLoadOnStartup(1);
        ctx.addServletMappingDecoded("/*", "dispatcherServlet");

        tomcat.start();
        System.out.println("Spring Framework thuan (mini Spring) dang chay tai http://localhost:" + port);
        System.out.println("Basic Auth cho POST/PUT/DELETE: admin / admin123");
        tomcat.getServer().await();
    }
}
