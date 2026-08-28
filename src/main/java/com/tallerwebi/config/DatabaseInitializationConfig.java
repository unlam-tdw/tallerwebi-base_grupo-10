package com.tallerwebi.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
public class DatabaseInitializationConfig {

  // Depends on the EntityManagerFactory to guarantee hbm2ddl created the schema
  // before data.sql runs.
  @Bean
  public DataSourceInitializer dataSourceInitializer(
    DataSource dataSource,
    EntityManagerFactory entityManagerFactory
  ) {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(new ClassPathResource("data.sql"));

    DataSourceInitializer initializer = new DataSourceInitializer();
    initializer.setDataSource(dataSource);
    initializer.setDatabasePopulator(populator);
    return initializer;
  }
}
