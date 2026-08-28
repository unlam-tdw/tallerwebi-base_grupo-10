package com.tallerwebi.e2e.views;

import com.microsoft.playwright.Page;

public class NewUserPage extends WebPage {

  public NewUserPage(Page page) {
    super(page);
  }

  public void typeEmail(String email) {
    this.typeIntoElement("#email", email);
  }

  public void typePassword(String password) {
    this.typeIntoElement("#password", password);
  }

  public void clickRegister() {
    this.clickElement("#btn-register");
  }

  public String getErrorMessage() {
    return this.getElementText("p.alert.alert-danger");
  }
}
