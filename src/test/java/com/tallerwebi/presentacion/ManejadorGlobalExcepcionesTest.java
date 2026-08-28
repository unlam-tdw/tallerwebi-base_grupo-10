package com.tallerwebi.presentacion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class ManejadorGlobalExcepcionesTest {

  private ManejadorGlobalExcepciones manejador = new ManejadorGlobalExcepciones();

  @Test
  public void usuarioExistenteDeberiaReRenderizarElFormularioDeRegistroConError() {
    // ejecucion
    ModelAndView modelAndView = manejador.manejarUsuarioExistente();

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("El email ya está registrado")
    );
    assertThat(
      modelAndView.getModel().get("datosNuevoUsuario"),
      instanceOf(DatosNuevoUsuario.class)
    );
  }

  @Test
  public void errorInesperadoDeberiaMostrarVistaDeErrorConMensajeGenerico() {
    // ejecucion
    ModelAndView modelAndView = manejador.manejarErrorInesperado(new RuntimeException("boom"));

    // validacion
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("error"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Ocurrió un error inesperado")
    );
  }
}
