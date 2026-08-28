package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

public class ControladorLoginTest {

  private ControladorLogin controladorLogin;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private ServicioLogin servicioLoginMock;
  private DatosNuevoUsuario datosNuevoUsuario;

  @BeforeEach
  public void init() {
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    servicioLoginMock = mock(ServicioLogin.class);
    controladorLogin = new ControladorLogin(servicioLoginMock);
    datosNuevoUsuario = new DatosNuevoUsuario("dami@unlam.com", "123456");
  }

  @Test
  public void loginConUsuarioYPasswordIncorrectosDeberiaLlevarALoginNuevamente() {
    // preparacion
    when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);
    DatosLogin datosLogin = new DatosLogin("dami@unlam.com", "123456");
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      datosLogin,
      "datosLogin"
    );

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(
      datosLogin,
      bindingResult,
      requestMock
    );

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Usuario o clave incorrecta")
    );
    verify(sessionMock, times(0)).setAttribute("ROL", "ADMIN");
  }

  @Test
  public void loginConUsuarioYPasswordCorrectosDeberiaLLevarAHome() {
    // preparacion
    Usuario usuarioEncontradoMock = mock(Usuario.class);
    when(usuarioEncontradoMock.getRol()).thenReturn("ADMIN");

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(servicioLoginMock.consultarUsuario(anyString(), anyString()))
      .thenReturn(usuarioEncontradoMock);
    DatosLogin datosLogin = new DatosLogin("dami@unlam.com", "123456");
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      datosLogin,
      "datosLogin"
    );

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(
      datosLogin,
      bindingResult,
      requestMock
    );

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
    verify(sessionMock, times(1)).setAttribute("ROL", usuarioEncontradoMock.getRol());
  }

  @Test
  public void loginConDatosInvalidosDeberiaReRenderizarLaVistaLogin() {
    // preparacion: el binding result tiene errores de validacion
    DatosLogin datosLogin = new DatosLogin("", "");
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      datosLogin,
      "datosLogin"
    );
    bindingResult.addError(new FieldError("datosLogin", "email", "El email es obligatorio"));

    // ejecucion
    ModelAndView modelAndView = controladorLogin.validarLogin(
      datosLogin,
      bindingResult,
      requestMock
    );

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Datos de login inválidos")
    );
    verify(servicioLoginMock, times(0)).consultarUsuario(anyString(), anyString());
  }

  @Test
  public void registrarmeSiUsuarioNoExisteDeberiaCrearUsuarioYVolverAlLogin() {
    // ejecucion
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      datosNuevoUsuario,
      "datosNuevoUsuario"
    );
    ModelAndView modelAndView = controladorLogin.registrarme(datosNuevoUsuario, bindingResult);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
    verify(servicioLoginMock, times(1)).registrar(captor.capture());
    assertThat(captor.getValue().getEmail(), equalToIgnoringCase("dami@unlam.com"));
    assertThat(captor.getValue().getPassword(), equalToIgnoringCase("123456"));
  }

  @Test
  public void registrarmeConDatosInvalidosDeberiaReRenderizarElFormulario() {
    // preparacion: el binding result tiene errores de validacion
    DatosNuevoUsuario datosInvalidos = new DatosNuevoUsuario("", "");
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      datosInvalidos,
      "datosNuevoUsuario"
    );
    bindingResult.addError(new FieldError("datosNuevoUsuario", "email", "El email es obligatorio"));

    // ejecucion
    ModelAndView modelAndView = controladorLogin.registrarme(datosInvalidos, bindingResult);

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Datos de registro inválidos")
    );
    verify(servicioLoginMock, times(0)).registrar(any(Usuario.class));
  }

  @Test
  public void registrarmeSiUsuarioExisteDeberiaPropagarExcepcion() {
    // preparacion: el servicio lanza UsuarioExistente (lo maneja el @ControllerAdvice)
    doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(any(Usuario.class));
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      datosNuevoUsuario,
      "datosNuevoUsuario"
    );

    // ejecucion y validacion
    assertThrows(
      UsuarioExistente.class,
      () -> controladorLogin.registrarme(datosNuevoUsuario, bindingResult)
    );
  }

  @Test
  public void errorEnRegistrarmeDeberiaPropagarExcepcion() {
    // preparacion: error inesperado en el servicio (lo maneja el @ControllerAdvice)
    doThrow(new RuntimeException()).when(servicioLoginMock).registrar(any(Usuario.class));
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      datosNuevoUsuario,
      "datosNuevoUsuario"
    );

    // ejecucion y validacion
    assertThrows(
      RuntimeException.class,
      () -> controladorLogin.registrarme(datosNuevoUsuario, bindingResult)
    );
  }

  @Test
  public void irALoginDeberiaRetornarVistaLoginConDatosLogin() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irALogin();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(modelAndView.getModel().get("datosLogin"), instanceOf(DatosLogin.class));
  }

  @Test
  public void nuevoUsuarioDeberiaRetornarVistaNuevoUsuarioConDatosVacios() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.nuevoUsuario();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("datosNuevoUsuario"),
      instanceOf(DatosNuevoUsuario.class)
    );
  }

  @Test
  public void irAHomeDeberiaRetornarVistaHome() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.irAHome();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
  }

  @Test
  public void inicioDeberiaRedirigirALogin() {
    // ejecucion
    ModelAndView modelAndView = controladorLogin.inicio();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }
}
