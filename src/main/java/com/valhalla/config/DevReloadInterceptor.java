package com.valhalla.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Exposes the live-reload token to every rendered view when dev reload is on.
 * The layout reads this request attribute and, if present, injects the polling
 * script that reloads the page as soon as the token changes.
 */
public final class DevReloadInterceptor implements HandlerInterceptor {

  static final String TOKEN_ATTRIBUTE = "devReloadToken";

  private final DevReloadController controller;

  public DevReloadInterceptor(DevReloadController controller) {
    this.controller = controller;
  }

  @Override
  public boolean preHandle(
    HttpServletRequest request,
    HttpServletResponse response,
    Object handler
  ) {
    request.setAttribute(TOKEN_ATTRIBUTE, controller.signature());
    return true;
  }
}
