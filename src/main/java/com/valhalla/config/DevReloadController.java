package com.valhalla.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dev-only live reload companion. Exposes a "signature" that changes whenever a
 * template changes or the application restarts, so the browser knows when to
 * reload itself. It is active only in development: the templates directory the
 * {@code jetty:run} setup serves in place exists at the repo root, while in the
 * packaged WAR (Docker) that path does not exist. When inactive the endpoint
 * answers 404 and no interceptor exposes the token, so pages render it disabled.
 */
@RestController
public class DevReloadController {

  private static final Path DEV_TEMPLATES_DIR = Path.of("src/main/webapp/WEB-INF/templates");

  private final Path templatesDir;
  private final String bootId;
  private final boolean enabled;

  public DevReloadController() {
    this(DEV_TEMPLATES_DIR, Files.exists(DEV_TEMPLATES_DIR));
  }

  DevReloadController(Path templatesDir, boolean enabled) {
    this.templatesDir = templatesDir;
    this.bootId = Long.toHexString(System.nanoTime());
    this.enabled = enabled;
  }

  @GetMapping("/reload/version")
  public ResponseEntity<String> currentToken() {
    return enabled ? ResponseEntity.ok(token()) : ResponseEntity.notFound().build();
  }

  String signature() {
    return enabled ? token() : null;
  }

  private String token() {
    return bootId + ":" + templatesSignature();
  }

  private String templatesSignature() {
    StringBuilder builder = new StringBuilder();
    List<Path> files;
    try (Stream<Path> stream = Files.walk(templatesDir)) {
      files =
        stream
          .filter(Files::isRegularFile)
          .filter(p -> p.getFileName().toString().endsWith(".html"))
          .sorted()
          .toList();
    } catch (IOException exception) {
      return "";
    }
    for (Path file : files) {
      try {
        builder.append(templatesDir.relativize(file));
        builder.append(file.toFile().lastModified());
        builder.append(file.toFile().length());
      } catch (Exception exception) {
        return "";
      }
    }
    return Integer.toString(builder.toString().hashCode());
  }
}
