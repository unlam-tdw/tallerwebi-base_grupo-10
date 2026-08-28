package com.tallerwebi.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Configuración JPA de tests: HSQLDB en memoria, solo cambia el DataSource y el dialecto. */
@Configuration
public class JpaTestConfig extends BaseConfiguracionJpa {

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
    dataSource.setUrl("jdbc:hsqldb:mem:db_");
    dataSource.setUsername("sa");
    dataSource.setPassword("");
    return dataSource;
  }

  @Override
  protected Properties jpaProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
    properties.setProperty("hibernate.show_sql", "true");
    properties.setProperty("hibernate.format_sql", "true");
    properties.setProperty("hibernate.hbm2ddl.auto", "create");
    return properties;
  }
}
