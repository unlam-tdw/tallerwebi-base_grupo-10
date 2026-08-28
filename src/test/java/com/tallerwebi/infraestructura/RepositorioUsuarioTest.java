package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerwebi.config.JpaTestConfig;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { JpaTestConfig.class })
public class RepositorioUsuarioTest {

  @Autowired
  private RepositorioUsuario repositorioUsuario;

  @Test
  @Transactional
  @Rollback
  public void deberiaGuardarUnNuevoUsuario() {
    String emailNuevoUsuario = "nuevo.usuario@test.com";
    // preparacion
    Usuario usuario = this.dadoQueTengoUnUsuario(emailNuevoUsuario, "1234", "USER");

    // ejecucion
    this.cuandoGuardoUnUsuario(usuario);

    // validacion
    this.entoncesSeGuardoElUsuario(emailNuevoUsuario, usuario);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaEncontrarUnUsuarioExistenteCuandoBuscoPorEmail() {
    String email = "test@test.com";
    Usuario usuario = this.dadoQueTengoUnUsuario(email, "123", "USER");
    this.dadoQueExisteElUsuario(usuario);

    Usuario obtenido = this.cuandoObtengoUnUsuarioPorEmail(email);

    this.entoncesElUsuarioObtenidoEsCorrecto(obtenido, usuario);
  }

  @Test
  @Transactional
  public void noDeberiaEncontrarUnUsuarioInexistenteCuandoBuscoPorEmail() {
    Usuario obtenido = this.cuandoObtengoUnUsuarioPorEmail("test@test.com");
    this.entoncesElUsuarioObtenidoEsNull(obtenido);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaModificarUnUsuarioExistente() {
    String email = "test@test.com";
    Usuario usuario = this.dadoQueTengoUnUsuario(email, "123", "USER");
    this.dadoQueExisteElUsuario(usuario);

    usuario.setPassword("4567");
    usuario.setActivo(true);
    usuario.setRol("ADMIN");

    this.cuandoModificoUnUsuario(usuario);

    Usuario obtenido = this.cuandoObtengoUnUsuarioPorEmail(email);
    this.entoncesElUsuarioObtenidoEsCorrecto(obtenido, usuario);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaLanzarUnaExcepcionAlIntentarModificarUnUsuarioInexistente() {
    Usuario usuario = this.dadoQueTengoUnUsuario("noexiste@test.com", "123", "USER");

    // Al no tener ID (no estar persistido), modificar debe lanzar UsuarioNoEncontrado.
    this.entoncesSeLanzaUnaUsuarioNoEncontrado(usuario);
  }

  @Test
  @Transactional
  @Rollback
  public void deberiaLanzarUnaExcepcionAlIntentarModificarUnUsuarioConIdInexistente() {
    Usuario usuario = this.dadoQueTengoUnUsuario("noexiste@test.com", "123", "USER");
    usuario.setId(999L);

    // Con ID pero sin registro asociado, modificar debe lanzar UsuarioNoEncontrado.
    this.entoncesSeLanzaUnaUsuarioNoEncontrado(usuario);
  }

  private Usuario dadoQueTengoUnUsuario(String email, String password, String rol) {
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword(password);
    usuario.setRol(rol);
    return usuario;
  }

  private void dadoQueExisteElUsuario(Usuario usuario) {
    this.repositorioUsuario.save(usuario);
  }

  private void cuandoGuardoUnUsuario(Usuario usuario) {
    this.repositorioUsuario.save(usuario);
  }

  private Usuario cuandoObtengoUnUsuarioPorEmail(String email) {
    return this.repositorioUsuario.findByEmail(email).orElse(null);
  }

  private void cuandoModificoUnUsuario(Usuario usuario) {
    this.repositorioUsuario.modificar(usuario);
  }

  private void entoncesSeGuardoElUsuario(String email, Usuario usuarioEsperado) {
    Usuario usuarioObtenido = this.repositorioUsuario.findByEmail(email).orElse(null);
    this.entoncesElUsuarioObtenidoEsCorrecto(usuarioEsperado, usuarioObtenido);
  }

  private void entoncesElUsuarioObtenidoEsCorrecto(
    Usuario usuarioObtenido,
    Usuario usuarioEsperado
  ) {
    assertThat(usuarioObtenido, is(not(nullValue())));
    assertThat(usuarioObtenido.getEmail(), is(equalTo(usuarioEsperado.getEmail())));
    assertThat(usuarioObtenido.getPassword(), is(equalTo(usuarioEsperado.getPassword())));
    assertThat(usuarioObtenido.getActivo(), is(equalTo(usuarioEsperado.getActivo())));
    assertThat(usuarioObtenido.getRol(), is(equalTo(usuarioEsperado.getRol())));
  }

  private void entoncesElUsuarioObtenidoEsNull(Usuario obtenido) {
    assertThat(obtenido, is(nullValue()));
  }

  private void entoncesSeLanzaUnaUsuarioNoEncontrado(Usuario usuario) {
    assertThrows(
      UsuarioNoEncontrado.class,
      () -> {
        this.cuandoModificoUnUsuario(usuario);
      }
    );
  }
}
