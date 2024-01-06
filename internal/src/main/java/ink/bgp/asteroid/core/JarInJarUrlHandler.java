package ink.bgp.asteroid.core;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.ivy.core.settings.TimeoutConstraint;
import org.apache.ivy.util.url.BasicURLHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLStreamHandler;

/* package-private */ class JarInJarUrlHandler extends BasicURLHandler {
  @Getter(lazy = true)
  private static final @NotNull URLStreamHandler jarInJarHandler = new ink.bgp.asteroid.loader.jar.Handler();

  @Override
  protected URL normalizeToURL(URL url) throws IOException {
    if ("jar".equals(url.getProtocol())) {
      return new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), jarInJarHandler());
    }
    return url;
  }
}
