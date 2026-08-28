package com.tallerwebi.domain;

import com.tallerwebi.domain.exception.UserAlreadyExists;
import com.tallerwebi.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public LoginServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User findUser(String email, String password) {
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
      return null;
    }
    return user;
  }

  @Override
  public void register(User user) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      throw new UserAlreadyExists();
    }
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    userRepository.save(user);
  }
}
