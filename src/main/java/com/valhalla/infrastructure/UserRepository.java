package com.valhalla.infrastructure;

import com.valhalla.domain.User;
import com.valhalla.domain.exception.UserNotFoundException;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);

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
