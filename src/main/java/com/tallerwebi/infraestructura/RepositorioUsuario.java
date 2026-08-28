package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioNoEncontrado;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
  Optional<Usuario> findByEmail(String email);

  default void modificar(Usuario usuario) {
    if (usuario.getId() == null) {
      throw new UsuarioNoEncontrado();
    }
    if (!existsById(usuario.getId())) {
      throw new UsuarioNoEncontrado();
    }
    save(usuario);
  }
}
