package com.tallerwebi.punta_a_punta;

import com.tallerwebi.config.ConfiguracionEntorno;
import java.io.IOException;

public class ReiniciarDB {

  public static void limpiarBaseDeDatos() {
    try {
      String dbHost = ConfiguracionEntorno.dbHost();
      String dbPort = ConfiguracionEntorno.dbPort();
      String dbName = ConfiguracionEntorno.dbName();
      String dbUser = ConfiguracionEntorno.dbUser();
      String dbPassword = ConfiguracionEntorno.dbPassword();

      // El hash BCrypt corresponde a la contraseña 'test' (prefijo $2a$10$).
      // Se escapan los '$' (\\$) para que bash no los interprete como variables.
      String hash = "\\$2a\\$10\\$ShOBUPfT5jLImCcQoWkM6edIQ3xjC6XYzgC7RDOPLqGiTRgHMkh2K";
      String sqlCommands =
        "DELETE FROM Usuario;\n" +
        "ALTER TABLE Usuario AUTO_INCREMENT = 1;\n" +
        "INSERT INTO Usuario(id, email, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', '" +
        hash +
        "', 'ADMIN', true);";

      String comando = String.format(
        "docker exec tallerwebi-mysql mysql -h %s -P %s -u %s -p%s %s -e \"%s\"",
        dbHost,
        dbPort,
        dbUser,
        dbPassword,
        dbName,
        sqlCommands
      );

      Process process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", comando });
      int exitCode = process.waitFor();

      if (exitCode == 0) {
        System.out.println("Base de datos limpiada exitosamente");
      } else {
        System.err.println("Error al limpiar la base de datos. Exit code: " + exitCode);
      }
    } catch (IOException | InterruptedException e) {
      System.err.println("Error ejecutando script de limpieza: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
