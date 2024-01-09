package ink.bgp.asteroid.core.injector;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public interface UrlInjector<T extends ClassLoader> extends FileInjector<T> {
  void addURL(final T classLoader, final @NotNull URL url);

  @Override
  @SneakyThrows
  default void addFile(final T classLoader, final @NotNull File jarFile) {
    addURL(classLoader, jarFile.toURI().toURL());
  }
}
