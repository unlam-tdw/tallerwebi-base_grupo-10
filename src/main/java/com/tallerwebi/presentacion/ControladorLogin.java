package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorLogin {

  private static final String VISTA_LOGIN = "login";
  private static final String VISTA_NUEVO_USUARIO = "nuevo-usuario";
  private static final String ATRIBUTO_DATOS_LOGIN = "datosLogin";
  private static final String ATRIBUTO_DATOS_NUEVO_USUARIO = "datosNuevoUsuario";

  private ServicioLogin servicioLogin;

  @Autowired
  public ControladorLogin(ServicioLogin servicioLogin) {
    this.servicioLogin = servicioLogin;
  }

  @RequestMapping("/login")
  public ModelAndView irALogin() {
    Map<String, Object> modelo = new ModelMap();
    modelo.put(ATRIBUTO_DATOS_LOGIN, new DatosLogin());
    return new ModelAndView(VISTA_LOGIN, modelo);
  }

  @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
  public ModelAndView validarLogin(
    @Valid @ModelAttribute(ATRIBUTO_DATOS_LOGIN) DatosLogin datosLogin,
    BindingResult bindingResult,
    HttpServletRequest request
  ) {
    if (bindingResult.hasErrors()) {
      Map<String, Object> model = new ModelMap();
      model.put(ATRIBUTO_DATOS_LOGIN, datosLogin);
      model.put("error", "Datos de login inválidos");
      return new ModelAndView(VISTA_LOGIN, model);
    }
    Usuario usuarioBuscado = servicioLogin.consultarUsuario(
      datosLogin.getEmail(),
      datosLogin.getPassword()
    );
    if (usuarioBuscado != null) {
      request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
      return new ModelAndView("redirect:/home");
    } else {
      Map<String, Object> model = new ModelMap();
      model.put(ATRIBUTO_DATOS_LOGIN, datosLogin);
      model.put("error", "Usuario o clave incorrecta");
      return new ModelAndView(VISTA_LOGIN, model);
    }
  }

  @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
  public ModelAndView registrarme(
    @Valid @ModelAttribute(ATRIBUTO_DATOS_NUEVO_USUARIO) DatosNuevoUsuario datosNuevoUsuario,
    BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) {
      Map<String, Object> model = new ModelMap();
      model.put(ATRIBUTO_DATOS_NUEVO_USUARIO, datosNuevoUsuario);
      model.put("error", "Datos de registro inválidos");
      return new ModelAndView(VISTA_NUEVO_USUARIO, model);
    }
    Usuario usuario = new Usuario();
    usuario.setEmail(datosNuevoUsuario.getEmail());
    usuario.setPassword(datosNuevoUsuario.getPassword());
    servicioLogin.registrar(usuario);
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
  public ModelAndView nuevoUsuario() {
    Map<String, Object> model = new ModelMap();
    model.put(ATRIBUTO_DATOS_NUEVO_USUARIO, new DatosNuevoUsuario());
    return new ModelAndView(VISTA_NUEVO_USUARIO, model);
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView irAHome() {
    return new ModelAndView("home");
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView inicio() {
    return new ModelAndView("redirect:/login");
  }
}
