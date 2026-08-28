package com.tallerwebi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.tallerwebi.config.JpaTestConfig;
import com.tallerwebi.domain.LoginService;
import com.tallerwebi.domain.User;
import com.tallerwebi.infrastructure.UserRepository;
import com.tallerwebi.integration.config.SpringWebTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, JpaTestConfig.class })
public class LoginControllerTest {

  private static final String LOGIN_EMAIL = "login@unlam.edu.ar";
  private static final String LOGIN_PASSWORD = "secure-password";

  private static final String ATTR_LOGIN_DATA = "loginData";
  private static final String ATTR_NEW_USER_DATA = "newUserData";
  private static final String BINDING_RESULT_LOGIN =
    "org.springframework.validation.BindingResult." + ATTR_LOGIN_DATA;
  private static final String BINDING_RESULT_NEW_USER =
    "org.springframework.validation.BindingResult." + ATTR_NEW_USER_DATA;

  @Autowired
  private WebApplicationContext wac;

  @Autowired
  private LoginService loginService;

  @Autowired
  private UserRepository userRepository;

  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    if (userRepository.findByEmail(LOGIN_EMAIL).isEmpty()) {
      User user = new User();
      user.setEmail(LOGIN_EMAIL);
      user.setPassword(LOGIN_PASSWORD);
      loginService.register(user);
    }
  }

  @Test
  public void shouldRedirectToLoginPageFromRoot() throws Exception {
    this.mockMvc.perform(get("/"))
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void shouldReturnLoginPageWithLoginRequest() throws Exception {
    this.mockMvc.perform(get("/login"))
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attributeExists(ATTR_LOGIN_DATA));
  }

  @Test
  public void shouldReturnNewUserPageWithNewUserRequest() throws Exception {
    this.mockMvc.perform(get("/new-user"))
      .andExpect(status().isOk())
      .andExpect(view().name("new-user"))
      .andExpect(model().attributeExists(ATTR_NEW_USER_DATA));
  }

  @Test
  public void shouldRedirectToHomeWhenCredentialsAreCorrect() throws Exception {
    this.mockMvc.perform(
        post("/validate-login").param("email", LOGIN_EMAIL).param("password", LOGIN_PASSWORD)
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/home"));
  }

  @Test
  public void shouldReRenderLoginWithErrorWhenPasswordIsWrong() throws Exception {
    this.mockMvc.perform(
        post("/validate-login").param("email", LOGIN_EMAIL).param("password", "wrong-password")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attribute("error", "Invalid email or password"))
      .andExpect(model().attributeExists(ATTR_LOGIN_DATA));
  }

  @Test
  public void shouldReRenderLoginWithValidationErrorsWhenEmailIsMissing() throws Exception {
    this.mockMvc.perform(post("/validate-login").param("password", LOGIN_PASSWORD))
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attribute("error", "Invalid email or password"))
      .andExpect(model().attributeExists(BINDING_RESULT_LOGIN));
  }

  @Test
  public void shouldReRenderLoginWithValidationErrorsWhenEmailIsInvalid() throws Exception {
    this.mockMvc.perform(
        post("/validate-login").param("email", "not-an-email").param("password", LOGIN_PASSWORD)
      )
      .andExpect(status().isOk())
      .andExpect(view().name("login"))
      .andExpect(model().attribute("error", "Invalid email or password"))
      .andExpect(model().attributeExists(BINDING_RESULT_LOGIN));
  }

  @Test
  public void shouldRedirectToLoginWhenRegisteringWithANewEmail() throws Exception {
    this.mockMvc.perform(
        post("/register").param("email", "new@unlam.edu.ar").param("password", "new-password")
      )
      .andExpect(status().is3xxRedirection())
      .andExpect(redirectedUrl("/login"));
  }

  @Test
  public void shouldReRenderNewUserWithErrorWhenEmailAlreadyExists() throws Exception {
    String duplicateEmail = "duplicate@unlam.edu.ar";
    this.mockMvc.perform(
        post("/register").param("email", duplicateEmail).param("password", LOGIN_PASSWORD)
      )
      .andExpect(status().is3xxRedirection());

    this.mockMvc.perform(
        post("/register").param("email", duplicateEmail).param("password", "another-password")
      )
      .andExpect(status().isOk())
      .andExpect(view().name("new-user"))
      .andExpect(model().attribute("error", "Email is already registered"));
  }

  @Test
  public void shouldReRenderNewUserWithValidationErrorsWhenInputIsInvalid() throws Exception {
    this.mockMvc.perform(post("/register").param("email", "not-an-email").param("password", "123"))
      .andExpect(status().isOk())
      .andExpect(view().name("new-user"))
      .andExpect(model().attribute("error", "Invalid registration data"))
      .andExpect(model().attributeExists(BINDING_RESULT_NEW_USER));
  }
}
