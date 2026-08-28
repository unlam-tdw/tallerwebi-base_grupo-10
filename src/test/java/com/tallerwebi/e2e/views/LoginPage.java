package com.tallerwebi.e2e.views;

import com.microsoft.playwright.Page;

public class LoginPage extends WebPage {

  public LoginPage(Page page) {
    super(page);
    page.navigate("localhost:8080/spring/login");
  }

  public String getNavbarText() {
    return this.getElementText("nav a.navbar-brand");
  }

  public String getErrorMessage() {
    return this.getElementText("p.alert.alert-danger");
  }

  public void typeEmail(String email) {
    this.typeIntoElement("#email", email);
  }

  public void typePassword(String password) {
    this.typeIntoElement("#password", password);
  }

  public void clickSignIn() {
    this.clickElement("#btn-login");
  }

  public void clickRegister() {
    this.clickElement("#btn-register");
  }
}
