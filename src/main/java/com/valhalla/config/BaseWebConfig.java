package com.valhalla.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

/** Shared MVC base between production and tests: only the concrete context differs. */
@Configuration
@EnableWebMvc
@Import({ SecurityConfig.class, ValidationConfig.class })
@ComponentScan(
  { "com.valhalla.presentation", "com.valhalla.domain", "com.valhalla.infrastructure" }
)
public abstract class BaseWebConfig implements WebMvcConfigurer {

  // Spring + Thymeleaf need this
  @Autowired
  private ApplicationContext applicationContext;

  @Override
  public void addResourceHandlers(final ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/dist/**").addResourceLocations("/resources/core/dist/");
  }

  // https://www.thymeleaf.org/doc/tutorials/3.0/thymeleafspring.html
  // Spring + Thymeleaf
  @Bean
  public SpringResourceTemplateResolver templateResolver() {
    // SpringResourceTemplateResolver automatically integrates with Spring's own
    // resource resolution infrastructure, which is highly recommended.
    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
    templateResolver.setApplicationContext(this.applicationContext);
    templateResolver.setPrefix("/WEB-INF/views/thymeleaf/");
    templateResolver.setSuffix(".html");
    // HTML is the default value, added here for the sake of clarity.
    templateResolver.setTemplateMode(TemplateMode.HTML);
    // Template cache is off so template edits appear on refresh (F5) without
    // restarting the server. This is the dev-friendly default for a taller.
    templateResolver.setCacheable(false);
    return templateResolver;
  }

  // Spring + Thymeleaf
  @Bean
  public SpringTemplateEngine templateEngine() {
    // SpringTemplateEngine automatically applies SpringStandardDialect and
    // enables Spring's own MessageSource message resolution mechanisms.
    SpringTemplateEngine templateEngine = new SpringTemplateEngine();
    templateEngine.setTemplateResolver(templateResolver());
    // Enabling the SpringEL compiler with Spring 4.2.4 or newer can
    // speed up execution in most scenarios, but might be incompatible
    // with specific cases when expressions in one template are reused
    // across different data types, so this flag is "false" by default
    // for safer backwards compatibility.
    templateEngine.setEnableSpringELCompiler(true);
    return templateEngine;
  }

  // Spring + Thymeleaf
  // Configure Thymeleaf View Resolver
  @Bean
  public ThymeleafViewResolver viewResolver() {
    ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
    viewResolver.setTemplateEngine(templateEngine());
    return viewResolver;
  }
}
