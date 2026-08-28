package com.tallerwebi.integracion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.config.JpaTestConfig;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.infraestructura.RepositorioUsuario;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, JpaTestConfig.class })
public class ControladorLoginTest {

  private static final String EMAIL_LOGIN = "login@unlam.edu.ar";
  private static final String CLAVE_LOGIN = "clave-segura";

  private static final String ATTR_DATOS_LOGIN = "datosLogin";
  private static final String ATTR_DATOS_NUEVO_USUARIO = "datosNuevoUsuario";
  private static final String BINDING_RESULT_LOGIN =
    "org.springframework.validation.BindingResult." + ATTR_DATOS_LOGIN;
  private static final String BINDING_RESULT_NUEVO_USUARIO =
    "org.springframework.validation.BindingResult." + ATTR_DATOS_NUEVO_USUARIO;

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private ServicioLogin servicioLogin;

  @Autowired
  private RepositorioUsuario repositorioUsuario;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    if (repositorioUsuario.findByEmail(EMAIL_LOGIN).isEmpty()) {
      Usuario usuario = new Usuario();
      usuario.setEmail(EMAIL_LOGIN);
      usuario.setPassword(CLAVE_LOGIN);
      servicioLogin.registrar(usuario);
    }
  }

  @Test
  public void debeRetornarVistaLoginConDatosLoginParaLaRutaRaiz() throws Exception {
    this.mockMvc.perform(get("/"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void debeRetornarVistaLoginConDatosLoginAlNavegarALogin() throws Exception {
    this.mockMvc.perform(get("/login"))
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attributeExists(ATTR_DATOS_LOGIN));
  }

  @Test
  public void debeRetornarVistaNuevoUsuarioConDatosNuevoUsuario() throws Exception {
    this.mockMvc.perform(get("/nuevo-usuario"))
      .andExpect(status().isOk())
      .andExpect(view().name("nuevo-usuario"))
      .andExpect(model().attributeExists(ATTR_DATOS_NUEVO_USUARIO));
  }

  @Test
  public void debeRedirigirAHomeAlValidarLoginConCredencialesCorrectas() throws Exception {
    this.mockMvc.perform(
        post("/validar-login").param("email", EMAIL_LOGIN).param("password", CLAVE_LOGIN)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/home"));
  }

  @Test
  public void debeReRenderizarLoginConErrorAlValidarLoginConClaveIncorrecta() throws Exception {
    this.mockMvc.perform(
        post("/validar-login").param("email", EMAIL_LOGIN).param("password", "clave-incorrecta")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attribute("error", "Usuario o clave incorrecta"))
      .andExpect(model().attributeExists(ATTR_DATOS_LOGIN));
  }

  @Test
  public void debeReRenderizarLoginConErroresDeValidacionAlFaltarEmail() throws Exception {
    this.mockMvc.perform(post("/validar-login").param("password", CLAVE_LOGIN))
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attribute("error", "Datos de login inválidos"))
      .andExpect(model().attributeExists(BINDING_RESULT_LOGIN));
  }

  @Test
  public void debeReRenderizarLoginConErroresDeValidacionAlEnviarEmailInvalido() throws Exception {
    this.mockMvc.perform(
        post("/validar-login").param("email", "no-es-un-email").param("password", CLAVE_LOGIN)
      )
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attribute("error", "Datos de login inválidos"))
      .andExpect(model().attributeExists(BINDING_RESULT_LOGIN));
  }

  @Test
  public void debeRedirigirALoginAlRegistrarseConUnEmailNuevo() throws Exception {
    this.mockMvc.perform(
        post("/registrarme").param("email", "nuevo@unlam.edu.ar").param("password", "clave-nueva")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void debeReRenderizarNuevoUsuarioConErrorAlRegistrarUnEmailExistente() throws Exception {
    String emailDuplicado = "duplicado@unlam.edu.ar";
    this.mockMvc.perform(
        post("/registrarme").param("email", emailDuplicado).param("password", CLAVE_LOGIN)
      )
      .andExpect(status().is3xxRedirection());

    this.mockMvc.perform(
        post("/registrarme").param("email", emailDuplicado).param("password", "otra-clave")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("nuevo-usuario"))
      .andExpect(model().attribute("error", "El email ya está registrado"));
  }

  @Test
  public void debeReRenderizarNuevoUsuarioConErroresDeValidacionAlRegistrarDatosInvalidos()
    throws Exception {
    this.mockMvc.perform(
        post("/registrarme").param("email", "no-es-un-email").param("password", "123")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("nuevo-usuario"))
      .andExpect(model().attribute("error", "Datos de registro inválidos"))
      .andExpect(model().attributeExists(BINDING_RESULT_NUEVO_USUARIO));
  }
}
