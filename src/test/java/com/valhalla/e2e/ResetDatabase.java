package com.valhalla.e2e;

import com.valhalla.config.EnvironmentConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Resets the database to a known state using JDBC directly (no external tools). */
public class ResetDatabase {

  private ResetDatabase() {}

  /** Deletes all users and seeds the default admin user via JDBC. */
  public static void cleanDatabase() {
    String bcryptHash = "$2a$10$ShOBUPfT5jLImCcQoWkM6edIQ3xjC6XYzgC7RDOPLqGiTRgHMkh2K";
    String[] statements = {
      "DELETE FROM users",
      "ALTER SEQUENCE users_id_seq RESTART WITH 1",
      "INSERT INTO users(email, password, role, active) " +
      "VALUES('test@unlam.edu.ar', '" +
      bcryptHash +
      "', 'ADMIN', TRUE) ON CONFLICT (email) DO NOTHING",
    };

    try (Connection connection = openConnection()) {
      for (String sql : statements) {
        try (Statement statement = connection.createStatement()) {
          statement.execute(sql);
        }
      }
      System.out.println("Database cleaned successfully");
    } catch (SQLException e) {
      System.err.println("Error cleaning the database: " + e.getMessage());
    }
  }

  private static Connection openConnection() throws SQLException {
    return DriverManager.getConnection(
      EnvironmentConfig.databaseUrl(),
      EnvironmentConfig.dbUser(),
      EnvironmentConfig.dbPassword()
    );
  }
}
