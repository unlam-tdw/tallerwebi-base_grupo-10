package com.tallerwebi.presentacion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DatosNuevoUsuario {

  @NotBlank(message = "El email es obligatorio")
  @Email(message = "El email no es válido")
  private String email;

  @NotBlank(message = "La clave es obligatoria")
  @Size(min = 6, message = "La clave debe tener al menos 6 caracteres")
  private String password;

  public DatosNuevoUsuario() {}

  public DatosNuevoUsuario(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
