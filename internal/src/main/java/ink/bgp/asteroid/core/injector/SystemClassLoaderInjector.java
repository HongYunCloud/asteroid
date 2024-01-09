package ink.bgp.asteroid.core.injector;

import bot.inker.acj.JvmHacker;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.jar.JarFile;

public final class SystemClassLoaderInjector implements UrlInjector<@NotNull ClassLoader> {
  public static final @NotNull SystemClassLoaderInjector INSTANCE = new SystemClassLoaderInjector();

  private SystemClassLoaderInjector() {
    //
  }

  @Override
  @SneakyThrows
  public void addFile(final @NotNull ClassLoader classLoader, @NotNull File file) {
    if(classLoader != ClassLoader.getSystemClassLoader()) {
      throw new IllegalStateException("target class loader is not system class loader");
    }
    JvmHacker.instrumentation().appendToSystemClassLoaderSearch(new JarFile(file));
  }

  @Override
  @SneakyThrows
  public void addURL(final @NotNull ClassLoader classLoader, @NotNull URL url) {
    if(classLoader != ClassLoader.getSystemClassLoader()) {
      throw new IllegalStateException("target class loader is not system class loader");
    }
    if ("jar".equals(url.getProtocol())) {
      String subUrl = url.getFile();
      int split = subUrl.indexOf('!');
      if (split > 0) {
        subUrl = subUrl.substring(0, split);
      }
      if(subUrl.indexOf(':', split + 1) > 0) {
        throw new IllegalStateException("not support jar in jar");
      }
      url = new URL(subUrl);
    }
    if ("file".equals(url.getProtocol())) {
      JvmHacker.instrumentation().appendToSystemClassLoaderSearch(new JarFile(url.getFile()));
    } else {
      throw new IllegalStateException("unsupported protocol " + url.getProtocol() + " in " + url);
    }
  }

  @Override
  public boolean isAcceptable(@NotNull ClassLoader classLoader) {
    return classLoader == ClassLoader.getSystemClassLoader();
  }
}
