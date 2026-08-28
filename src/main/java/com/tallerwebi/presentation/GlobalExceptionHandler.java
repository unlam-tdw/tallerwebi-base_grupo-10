package com.tallerwebi.presentation;

import com.tallerwebi.domain.exception.UserAlreadyExists;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = Logger.getLogger(GlobalExceptionHandler.class.getName());

  @ExceptionHandler(UserAlreadyExists.class)
  public ModelAndView handleUserAlreadyExists() {
    Map<String, Object> model = new ModelMap();
    model.put("newUserData", new NewUserRequest());
    model.put("error", "Email is already registered");
    return new ModelAndView("new-user", model);
  }

  @ExceptionHandler(Exception.class)
  public ModelAndView handleUnexpectedError(Exception ex) {
    LOGGER.log(Level.SEVERE, "Unhandled error", ex);
    Map<String, Object> model = new ModelMap();
    model.put("error", "An unexpected error occurred");
    return new ModelAndView("error", model);
  }
}
