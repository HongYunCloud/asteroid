package ink.bgp.asteroid.core.injector;

import bot.inker.acj.JvmHacker;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.net.URLClassLoader;

public final class UrlClassLoaderInjector implements UrlInjector<@NotNull URLClassLoader> {
  private static final @NotNull MethodHandle addUrlHandler = loadAddUrlHandler();

  public static final @NotNull UrlClassLoaderInjector INSTANCE = new UrlClassLoaderInjector();

  private UrlClassLoaderInjector() {
    //
  }

  @SneakyThrows
  private static @NotNull MethodHandle loadAddUrlHandler() {
    return JvmHacker.lookup()
        .findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, URL.class));
  }

  @Override
  @SneakyThrows
  public void addURL(@NotNull URLClassLoader urlClassLoader, @NotNull URL url) {
    addUrlHandler.invokeExact(urlClassLoader, url);
  }
}
