package com.tallerwebi.dominio;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.infraestructura.RepositorioUsuario;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class ServicioLoginTest {

  private ServicioLogin servicioLogin;
  private RepositorioUsuario repositorioUsuarioMock;
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  public void init() {
    this.repositorioUsuarioMock = mock(RepositorioUsuario.class);
    this.passwordEncoder = new BCryptPasswordEncoder();
    this.servicioLogin = new ServicioLoginImpl(this.repositorioUsuarioMock, this.passwordEncoder);
  }

  @Test
  public void consultarUsuarioConPasswordCorrectaDeberiaDevolverElUsuario() {
    // preparacion
    String email = "test@test.com";
    String password = "clave123";
    Usuario usuarioEsperado = new Usuario();
    usuarioEsperado.setEmail(email);
    usuarioEsperado.setPassword(passwordEncoder.encode(password));
    when(this.repositorioUsuarioMock.findByEmail(email)).thenReturn(Optional.of(usuarioEsperado));

    // ejecucion
    Usuario usuarioObtenido = this.servicioLogin.consultarUsuario(email, password);

    // validacion
    assertThat(usuarioObtenido, equalTo(usuarioEsperado));
    verify(this.repositorioUsuarioMock, times(1)).findByEmail(email);
  }

  @Test
  public void consultarUsuarioConPasswordIncorrectaDeberiaDevolverNull() {
    // preparacion
    String email = "test@test.com";
    Usuario usuarioEsperado = new Usuario();
    usuarioEsperado.setEmail(email);
    usuarioEsperado.setPassword(passwordEncoder.encode("claveCorrecta"));
    when(this.repositorioUsuarioMock.findByEmail(email)).thenReturn(Optional.of(usuarioEsperado));

    // ejecucion
    Usuario usuarioObtenido = this.servicioLogin.consultarUsuario(email, "claveIncorrecta");

    // validacion
    assertThat(usuarioObtenido, is(nullValue()));
  }

  @Test
  public void consultarUsuarioInexistenteDeberiaDevolverNull() {
    // preparacion
    String email = "test@test.com";
    when(this.repositorioUsuarioMock.findByEmail(email)).thenReturn(Optional.empty());

    // ejecucion
    Usuario usuarioObtenido = this.servicioLogin.consultarUsuario(email, "clave123");

    // validacion
    assertThat(usuarioObtenido, is(nullValue()));
  }

  @Test
  public void registrarUsuarioSiNoExisteDeberiaGuardarloConPasswordEncriptada() {
    // preparacion
    String email = "nuevo@test.com";
    String password = "clave123";
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword(password);
    when(this.repositorioUsuarioMock.findByEmail(email)).thenReturn(Optional.empty());

    // ejecucion
    this.servicioLogin.registrar(usuario);

    // validacion
    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
    verify(this.repositorioUsuarioMock, times(1)).save(captor.capture());
    Usuario guardado = captor.getValue();
    assertThat(this.passwordEncoder.matches(password, guardado.getPassword()), is(true));
    assertThat(guardado.getPassword(), not(equalTo(password)));
  }

  @Test
  public void registrarUsuarioSiExisteDeberiaLanzarExcepcion() {
    // preparacion
    String email = "existe@test.com";
    Usuario usuario = new Usuario();
    usuario.setEmail(email);
    usuario.setPassword("clave123");
    when(this.repositorioUsuarioMock.findByEmail(email)).thenReturn(Optional.of(new Usuario()));

    // ejecucion y validacion
    assertThrows(UsuarioExistente.class, () -> this.servicioLogin.registrar(usuario));
    verify(this.repositorioUsuarioMock, times(0)).save(any(Usuario.class));
  }

  @Test
  public void registrarUsuarioConEmailExistentePeroDistintaPasswordDeberiaLanzarExcepcion() {
    // preparacion: existe un usuario con ese email y otra contraseña
    String email = "existe@test.com";
    Usuario existente = new Usuario();
    existente.setEmail(email);
    existente.setPassword(passwordEncoder.encode("claveExistente"));
    when(this.repositorioUsuarioMock.findByEmail(email)).thenReturn(Optional.of(existente));

    Usuario nuevo = new Usuario();
    nuevo.setEmail(email);
    nuevo.setPassword("claveDistinta");

    // ejecucion y validacion: el alta se rechaza por email existente
    assertThrows(UsuarioExistente.class, () -> this.servicioLogin.registrar(nuevo));
    verify(this.repositorioUsuarioMock, times(0)).save(any(Usuario.class));
  }
}
