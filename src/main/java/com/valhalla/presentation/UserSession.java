package com.valhalla.presentation;

import java.util.Objects;

public class UserSession {

  private final String email;
  private final String role;

  public UserSession(String email, String role) {
    this.email = email;
    this.role = role;
  }

  public String getEmail() {
    return email;
  }

  public String getRole() {
    return role;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    UserSession that = (UserSession) other;
    return Objects.equals(email, that.email) && Objects.equals(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, role);
  }
}
