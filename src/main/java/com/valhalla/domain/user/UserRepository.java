package com.valhalla.domain.user;

import com.valhalla.domain.exception.UserNotFoundException;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
  List<User> findAll();
  Optional<User> findById(Long id);
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean existsById(Long id);
  void save(User user);
  void deleteById(Long id);

  default void update(User user) {
    if (user.getId() == null) {
      throw new UserNotFoundException();
    }
    if (!existsById(user.getId())) {
      throw new UserNotFoundException();
    }
    save(user);
  }
}
