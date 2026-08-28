package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.infraestructura.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

  private RepositorioUsuario repositorioUsuario;
  private PasswordEncoder passwordEncoder;

  @Autowired
  public ServicioLoginImpl(RepositorioUsuario repositorioUsuario, PasswordEncoder passwordEncoder) {
    this.repositorioUsuario = repositorioUsuario;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Usuario consultarUsuario(String email, String password) {
    Usuario usuario = repositorioUsuario.findByEmail(email).orElse(null);
    if (usuario == null || !passwordEncoder.matches(password, usuario.getPassword())) {
      return null;
    }
    return usuario;
  }

  @Override
  public void registrar(Usuario usuario) {
    if (repositorioUsuario.findByEmail(usuario.getEmail()).isPresent()) {
      throw new UsuarioExistente();
    }
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    repositorioUsuario.save(usuario);
  }
}
