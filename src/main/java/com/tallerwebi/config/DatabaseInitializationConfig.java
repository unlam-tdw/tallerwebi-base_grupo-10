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

  // Depende del EntityManagerFactory para garantizar que hbm2ddl creó el esquema
  // antes de ejecutar data.sql.
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
