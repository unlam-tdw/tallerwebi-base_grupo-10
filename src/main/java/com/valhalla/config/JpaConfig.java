package com.valhalla.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Production JPA configuration: MySQL with parameters from environment variables. */
@Configuration
public class JpaConfig extends BaseJpaConfig {

  @Bean
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
    dataSource.setUrl(EnvironmentConfig.databaseUrl());
    dataSource.setUsername(EnvironmentConfig.dbUser());
    dataSource.setPassword(EnvironmentConfig.dbPassword());
    return dataSource;
  }

  @Override
  protected Properties jpaProperties() {
    Properties properties = new Properties();
    properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    properties.setProperty("hibernate.show_sql", "true");
    properties.setProperty("hibernate.format_sql", "true");
    properties.setProperty("hibernate.hbm2ddl.auto", "update");
    properties.setProperty("hibernate.connection.characterEncoding", "utf8");
    properties.setProperty("hibernate.connection.CharSet", "utf8");
    properties.setProperty("hibernate.connection.useUnicode", "true");
    return properties;
  }
}
