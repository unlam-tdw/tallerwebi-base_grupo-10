package com.tallerwebi.domain;

public interface LoginService {
  User findUser(String email, String password);
  void register(User user);
}
