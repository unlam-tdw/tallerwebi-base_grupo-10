package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ManejadorGlobalExcepciones {

  private static final Logger LOGGER = Logger.getLogger(ManejadorGlobalExcepciones.class.getName());

  @ExceptionHandler(UsuarioExistente.class)
  public ModelAndView manejarUsuarioExistente() {
    Map<String, Object> model = new ModelMap();
    model.put("datosNuevoUsuario", new DatosNuevoUsuario());
    model.put("error", "El email ya está registrado");
    return new ModelAndView("nuevo-usuario", model);
  }

  @ExceptionHandler(Exception.class)
  public ModelAndView manejarErrorInesperado(Exception ex) {
    LOGGER.log(Level.SEVERE, "Error no controlado", ex);
    Map<String, Object> model = new ModelMap();
    model.put("error", "Ocurrió un error inesperado");
    return new ModelAndView("error", model);
  }
}
