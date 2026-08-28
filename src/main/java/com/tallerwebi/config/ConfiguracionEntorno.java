package com.tallerwebi.config;

/** Centraliza la resolución de variables de entorno con valores por defecto. */
public final class ConfiguracionEntorno {

  private static final String VARIABLE_DB_HOST = "DB_HOST";
  private static final String VARIABLE_DB_PORT = "DB_PORT";
  private static final String VARIABLE_DB_NAME = "DB_NAME";
  private static final String VARIABLE_DB_USER = "DB_USER";
  private static final String VARIABLE_DB_PASSWORD = "DB_PASSWORD";

  private static final String DEFAULT_DB_HOST = "localhost";
  private static final String DEFAULT_DB_PORT = "3306";
  private static final String DEFAULT_DB_NAME = "tallerwebi";
  private static final String DEFAULT_DB_USER = "user";
  private static final String DEFAULT_DB_PASSWORD = "user";

  private ConfiguracionEntorno() {}

  public static String dbHost() {
    return valorDeEntorno(VARIABLE_DB_HOST, DEFAULT_DB_HOST);
  }

  public static String dbPort() {
    return valorDeEntorno(VARIABLE_DB_PORT, DEFAULT_DB_PORT);
  }

  public static String dbName() {
    return valorDeEntorno(VARIABLE_DB_NAME, DEFAULT_DB_NAME);
  }

  public static String dbUser() {
    return valorDeEntorno(VARIABLE_DB_USER, DEFAULT_DB_USER);
  }

  public static String dbPassword() {
    return valorDeEntorno(VARIABLE_DB_PASSWORD, DEFAULT_DB_PASSWORD);
  }

  public static String urlBaseDeDatos() {
    return String.format(
      "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
      dbHost(),
      dbPort(),
      dbName()
    );
  }

  private static String valorDeEntorno(String variable, String valorPorDefecto) {
    String valor = System.getenv(variable);
    return valor == null ? valorPorDefecto : valor;
  }
}
