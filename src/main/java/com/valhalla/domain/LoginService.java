package com.valhalla.domain;

public interface LoginService {
  User findUser(String email, String password);
  void register(String email, String password);
}
