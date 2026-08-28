package com.tallerwebi.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Base JPA compartida entre producción y tests: solo difieren DataSource y dialecto. */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.tallerwebi.infraestructura")
public abstract class BaseConfiguracionJpa {

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean entityManagerFactory =
      new LocalContainerEntityManagerFactoryBean();
    entityManagerFactory.setDataSource(dataSource);
    entityManagerFactory.setPackagesToScan("com.tallerwebi.dominio");
    entityManagerFactory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    entityManagerFactory.setJpaProperties(jpaProperties());
    return entityManagerFactory;
  }

  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  protected abstract Properties jpaProperties();
}
