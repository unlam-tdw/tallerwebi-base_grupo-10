package com.valhalla.presentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  public void init() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  public void shouldReRenderRegistrationFormWithErrorWhenEmailAlreadyExists() {
    ModelAndView modelAndView = handler.handleUserAlreadyExists();
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("pages/auth/new-user"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Email is already registered")
    );
    assertThat(modelAndView.getModel().get("newUserData"), instanceOf(NewUserRequest.class));
  }

  @Test
  public void shouldShowErrorViewWithGenericMessage() {
    ModelAndView modelAndView = handler.handleUnexpectedError(new RuntimeException("boom"));
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("pages/error"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("An unexpected error occurred")
    );
  }
}
