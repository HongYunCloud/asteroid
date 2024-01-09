package ink.bgp.asteroid.api.classloader;

import org.jetbrains.annotations.NotNull;

import java.net.URL;

public interface AccessibleUrlPath {
  @NotNull URL @NotNull [] getURLs();
  void addURL(final @NotNull URL url);
}
