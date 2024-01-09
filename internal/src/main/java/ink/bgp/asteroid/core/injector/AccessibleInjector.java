package ink.bgp.asteroid.core.injector;

import ink.bgp.asteroid.api.classloader.AccessibleUrlPath;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public final class AccessibleInjector implements UrlInjector<@NotNull ClassLoader> {
  public static final @NotNull AccessibleInjector INSTANCE = new AccessibleInjector();

  private AccessibleInjector() {
    //
  }

  @Override
  public void addURL(@NotNull ClassLoader classLoader, @NotNull URL url) {
    ((AccessibleUrlPath) classLoader).addURL(url);
  }
}
