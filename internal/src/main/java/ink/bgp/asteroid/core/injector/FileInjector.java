package ink.bgp.asteroid.core.injector;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;

public interface FileInjector<T extends ClassLoader> {
  void addFile(final T classLoader, final @NotNull File jarFile);

  default boolean isAcceptable(final T classLoader) {
    return true;
  }
}
