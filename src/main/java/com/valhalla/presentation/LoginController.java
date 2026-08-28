package com.valhalla.presentation;

import com.valhalla.domain.LoginService;
import com.valhalla.domain.User;
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
public class LoginController {

  private static final String VIEW_LOGIN = "login";
  private static final String VIEW_NEW_USER = "new-user";
  private static final String ATTR_LOGIN_DATA = "loginData";
  private static final String ATTR_NEW_USER_DATA = "newUserData";

  private final LoginService loginService;

  @Autowired
  public LoginController(LoginService loginService) {
    this.loginService = loginService;
  }

  @RequestMapping("/login")
  public ModelAndView showLogin() {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_LOGIN_DATA, new LoginRequest());
    return new ModelAndView(VIEW_LOGIN, model);
  }

  @RequestMapping(path = "/validate-login", method = RequestMethod.POST)
  public ModelAndView validateLogin(
    @Valid @ModelAttribute(ATTR_LOGIN_DATA) LoginRequest loginData,
    BindingResult bindingResult,
    HttpServletRequest request
  ) {
    if (bindingResult.hasErrors()) {
      Map<String, Object> model = new ModelMap();
      model.put(ATTR_LOGIN_DATA, loginData);
      model.put("error", "Invalid email or password");
      return new ModelAndView(VIEW_LOGIN, model);
    }
    User foundUser = loginService.findUser(loginData.getEmail(), loginData.getPassword());
    if (foundUser != null) {
      request.getSession().setAttribute("ROLE", foundUser.getRole());
      return new ModelAndView("redirect:/home");
    } else {
      Map<String, Object> model = new ModelMap();
      model.put(ATTR_LOGIN_DATA, loginData);
      model.put("error", "Invalid email or password");
      return new ModelAndView(VIEW_LOGIN, model);
    }
  }

  @RequestMapping(path = "/register", method = RequestMethod.POST)
  public ModelAndView register(
    @Valid @ModelAttribute(ATTR_NEW_USER_DATA) NewUserRequest newUserData,
    BindingResult bindingResult
  ) {
    if (bindingResult.hasErrors()) {
      Map<String, Object> model = new ModelMap();
      model.put(ATTR_NEW_USER_DATA, newUserData);
      model.put("error", "Invalid registration data");
      return new ModelAndView(VIEW_NEW_USER, model);
    }
    User user = new User();
    user.setEmail(newUserData.getEmail());
    user.setPassword(newUserData.getPassword());
    loginService.register(user);
    return new ModelAndView("redirect:/login");
  }

  @RequestMapping(path = "/new-user", method = RequestMethod.GET)
  public ModelAndView showNewUser() {
    Map<String, Object> model = new ModelMap();
    model.put(ATTR_NEW_USER_DATA, new NewUserRequest());
    return new ModelAndView(VIEW_NEW_USER, model);
  }

  @RequestMapping(path = "/home", method = RequestMethod.GET)
  public ModelAndView showHome() {
    return new ModelAndView("home");
  }

  @RequestMapping(path = "/", method = RequestMethod.GET)
  public ModelAndView index() {
    return new ModelAndView("redirect:/login");
  }
}
