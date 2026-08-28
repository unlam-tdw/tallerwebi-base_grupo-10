package com.tallerwebi.presentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

public class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  public void shouldReRenderRegistrationFormWithErrorWhenEmailAlreadyExists() {
    // when
    ModelAndView modelAndView = handler.handleUserAlreadyExists();

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("new-user"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Email is already registered")
    );
    assertThat(modelAndView.getModel().get("newUserData"), instanceOf(NewUserRequest.class));
  }

  @Test
  public void shouldShowErrorViewWithGenericMessage() {
    // when
    ModelAndView modelAndView = handler.handleUnexpectedError(new RuntimeException("boom"));

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("error"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("An unexpected error occurred")
    );
  }
}
