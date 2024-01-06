package ink.bgp.asteroid.core;

import org.apache.ivy.util.url.URLHandlerRegistry;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.Instrumentation;

public final class AsteroidCoreLauncher implements Runnable {
  @Override
  public void run() {
    URLHandlerRegistry.setDefault(new JarInJarUrlHandler());

    new AsteroidCore();
  }
}
