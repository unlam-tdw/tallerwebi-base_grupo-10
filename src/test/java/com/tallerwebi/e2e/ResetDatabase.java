package com.tallerwebi.e2e;

import com.tallerwebi.config.EnvironmentConfig;
import java.io.IOException;

public class ResetDatabase {

  public static void cleanDatabase() {
    try {
      String dbHost = EnvironmentConfig.dbHost();
      String dbPort = EnvironmentConfig.dbPort();
      String dbName = EnvironmentConfig.dbName();
      String dbUser = EnvironmentConfig.dbUser();
      String dbPassword = EnvironmentConfig.dbPassword();

      // The BCrypt hash corresponds to the password 'test' (prefix $2a$10$).
      // The '$' chars are escaped (\\$) so bash does not interpret them as variables.
      String hash = "\\$2a\\$10\\$ShOBUPfT5jLImCcQoWkM6edIQ3xjC6XYzgC7RDOPLqGiTRgHMkh2K";
      String sqlCommands =
        "DELETE FROM users;\n" +
        "ALTER TABLE users AUTO_INCREMENT = 1;\n" +
        "INSERT INTO users(id, email, password, role, active) VALUES(null, 'test@unlam.edu.ar', '" +
        hash +
        "', 'ADMIN', true);";

      String command = String.format(
        "docker exec tallerwebi-mysql mysql -h %s -P %s -u %s -p%s %s -e \"%s\"",
        dbHost,
        dbPort,
        dbUser,
        dbPassword,
        dbName,
        sqlCommands
      );

      Process process = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", command });
      int exitCode = process.waitFor();

      if (exitCode == 0) {
        System.out.println("Database cleaned successfully");
      } else {
        System.err.println("Error cleaning the database. Exit code: " + exitCode);
      }
    } catch (IOException | InterruptedException e) {
      System.err.println("Error running cleanup script: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
