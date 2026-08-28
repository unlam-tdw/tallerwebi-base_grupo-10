package com.valhalla.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionInterceptor implements HandlerInterceptor {

  public static final String USER_SESSION = "userSession";

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) throws IOException {
    HttpSession session = request.getSession(false);
    if (session != null && session.getAttribute(USER_SESSION) != null) {
      return true;
    }
    response.sendRedirect(request.getContextPath() + "/login");
    return false;
  }
}
