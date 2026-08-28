package com.valhalla.presentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.LoginService;
import com.valhalla.domain.User;
import com.valhalla.domain.exception.UserAlreadyExists;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.ModelAndView;

public class LoginControllerTest {

  private LoginController controller;
  private HttpServletRequest requestMock;
  private HttpSession sessionMock;
  private LoginService loginServiceMock;
  private NewUserRequest newUserData;

  @BeforeEach
  public void init() {
    requestMock = mock(HttpServletRequest.class);
    sessionMock = mock(HttpSession.class);
    loginServiceMock = mock(LoginService.class);
    controller = new LoginController(loginServiceMock);
    newUserData = new NewUserRequest("dami@unlam.com", "123456");
  }

  @Test
  public void shouldReturnToLoginWhenCredentialsAreWrong() {
    // given
    when(loginServiceMock.findUser(anyString(), anyString())).thenReturn(null);
    LoginRequest loginData = new LoginRequest("dami@unlam.com", "123456");
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(loginData, "loginData");

    // when
    ModelAndView modelAndView = controller.validateLogin(loginData, bindingResult, requestMock);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Invalid email or password")
    );
    verify(sessionMock, times(0)).setAttribute(anyString(), any());
  }

  @Test
  public void shouldGoToHomeAndStoreSessionWhenCredentialsAreCorrect() {
    // given
    User foundUserMock = mock(User.class);
    when(foundUserMock.getEmail()).thenReturn("dami@unlam.com");
    when(foundUserMock.getRole()).thenReturn("ADMIN");

    when(requestMock.getSession()).thenReturn(sessionMock);
    when(loginServiceMock.findUser(anyString(), anyString())).thenReturn(foundUserMock);
    LoginRequest loginData = new LoginRequest("dami@unlam.com", "123456");
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(loginData, "loginData");

    // when
    ModelAndView modelAndView = controller.validateLogin(loginData, bindingResult, requestMock);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
    ArgumentCaptor<UserSession> captor = ArgumentCaptor.forClass(UserSession.class);
    verify(sessionMock, times(1))
      .setAttribute(eq(SessionInterceptor.USER_SESSION), captor.capture());
    assertThat(captor.getValue().getEmail(), equalToIgnoringCase("dami@unlam.com"));
    assertThat(captor.getValue().getRole(), equalToIgnoringCase("ADMIN"));
  }

  @Test
  public void shouldReRenderLoginPageWhenInputIsInvalid() {
    // given: the binding result has validation errors
    LoginRequest loginData = new LoginRequest("", "");
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(loginData, "loginData");
    bindingResult.addError(new FieldError("loginData", "email", "Email is required"));

    // when
    ModelAndView modelAndView = controller.validateLogin(loginData, bindingResult, requestMock);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Invalid email or password")
    );
    verify(loginServiceMock, times(0)).findUser(anyString(), anyString());
  }

  @Test
  public void shouldCreateUserAndReturnToLoginWhenEmailIsAvailable() {
    // when
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      newUserData,
      "newUserData"
    );
    ModelAndView modelAndView = controller.register(newUserData, bindingResult);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(loginServiceMock, times(1)).register("dami@unlam.com", "123456");
  }

  @Test
  public void shouldReRenderRegistrationFormWhenInputIsInvalid() {
    // given: the binding result has validation errors
    NewUserRequest invalidData = new NewUserRequest("", "");
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      invalidData,
      "newUserData"
    );
    bindingResult.addError(new FieldError("newUserData", "email", "Email is required"));

    // when
    ModelAndView modelAndView = controller.register(invalidData, bindingResult);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("new-user"));
    assertThat(
      modelAndView.getModel().get("error").toString(),
      equalToIgnoringCase("Invalid registration data")
    );
    verify(loginServiceMock, times(0)).register(anyString(), anyString());
  }

  @Test
  public void shouldPropagateExceptionWhenEmailAlreadyExists() {
    // given: the service throws UserAlreadyExists (handled by @ControllerAdvice)
    doThrow(UserAlreadyExists.class).when(loginServiceMock).register(anyString(), anyString());

    // when and then
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      newUserData,
      "newUserData"
    );
    assertThrows(UserAlreadyExists.class, () -> controller.register(newUserData, bindingResult));
  }

  @Test
  public void shouldPropagateExceptionOnUnexpectedRegistrationError() {
    // given: unexpected service error (handled by @ControllerAdvice)
    doThrow(new RuntimeException()).when(loginServiceMock).register(anyString(), anyString());

    // when and then
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(
      newUserData,
      "newUserData"
    );
    assertThrows(RuntimeException.class, () -> controller.register(newUserData, bindingResult));
  }

  @Test
  public void shouldReturnLoginPageWithLoginRequest() {
    // when
    ModelAndView modelAndView = controller.showLogin();

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
    assertThat(modelAndView.getModel().get("loginData"), instanceOf(LoginRequest.class));
  }

  @Test
  public void shouldReturnNewUserPageWithEmptyData() {
    // when
    ModelAndView modelAndView = controller.showNewUser();

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("new-user"));
    assertThat(modelAndView.getModel().get("newUserData"), instanceOf(NewUserRequest.class));
  }

  @Test
  public void shouldReturnHomeViewWithUserWhenSessionExists() {
    // given
    UserSession sessionUser = new UserSession("dami@unlam.com", "ADMIN");
    when(sessionMock.getAttribute(SessionInterceptor.USER_SESSION)).thenReturn(sessionUser);

    // when
    ModelAndView modelAndView = controller.showHome(sessionMock);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
    assertThat(modelAndView.getModel().get("user"), equalTo(sessionUser));
  }

  @Test
  public void shouldRedirectToLoginWhenNoSessionUser() {
    // given
    when(sessionMock.getAttribute(SessionInterceptor.USER_SESSION)).thenReturn(null);

    // when
    ModelAndView modelAndView = controller.showHome(sessionMock);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }

  @Test
  public void shouldInvalidateSessionAndRedirectToLoginOnLogout() {
    // given
    when(requestMock.getSession(false)).thenReturn(sessionMock);

    // when
    ModelAndView modelAndView = controller.logout(requestMock);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(sessionMock, times(1)).invalidate();
  }

  @Test
  public void shouldRedirectToLoginOnLogoutWhenNoSession() {
    // given
    when(requestMock.getSession(false)).thenReturn(null);

    // when
    ModelAndView modelAndView = controller.logout(requestMock);

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    verify(sessionMock, times(0)).invalidate();
  }

  @Test
  public void shouldRedirectToLoginFromRoot() {
    // when
    ModelAndView modelAndView = controller.index();

    // then
    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
  }
}
