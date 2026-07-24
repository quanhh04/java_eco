package com.demo.securities.springfw.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Setup Hibernate/JPA thủ công — không qua spring-boot-starter-data-jpa. Đây chính
 * là phần Spring Boot tự động hóa hộ khi có starter đó trên classpath: tự tạo
 * DataSource từ application.properties, tự tạo EntityManagerFactory + JpaTransactionManager,
 * tự đăng ký PersistenceAnnotationBeanPostProcessor để @PersistenceContext hoạt động.
 */
@Configuration
@EnableTransactionManagement
public class JpaConfig {

    @Bean
    public static PersistenceAnnotationBeanPostProcessor persistenceAnnotationBeanPostProcessor() {
        return new PersistenceAnnotationBeanPostProcessor();
    }

    @Bean
    public DataSource dataSource() throws IOException {
        Properties props = loadDbProperties();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(props.getProperty("db.url"));
        dataSource.setUsername(props.getProperty("db.user"));
        dataSource.setPassword(props.getProperty("db.password"));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) throws IOException {
        Properties dbProps = loadDbProperties();

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.demo.securities.springfw.entity");
        // Set truong minh thay vi de Hibernate/JPA tu do qua ServiceLoader -
        // cung loai rui ro classloader/reflection da gap voi DriverManager va Spring-WS.
        emf.setPersistenceProviderClass(HibernatePersistenceProvider.class);

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProps = new Properties();
        jpaProps.setProperty("hibernate.hbm2ddl.auto", "none");
        jpaProps.setProperty("hibernate.show_sql", "true");
        jpaProps.setProperty("hibernate.default_schema", dbProps.getProperty("db.schema", "public"));
        emf.setJpaProperties(jpaProps);

        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private static Properties loadDbProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Path.of("db.properties"))) {
            props.load(in);
        }
        return props;
    }
}
