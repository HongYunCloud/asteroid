package ink.bgp.asteroid.core.injector;

import bot.inker.acj.JvmHacker;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.util.jar.JarFile;

public final class BootstrapClassLoaderInjector implements UrlInjector<@NotNull ClassLoader> {
  public static final @NotNull BootstrapClassLoaderInjector INSTANCE = new BootstrapClassLoaderInjector();

  private BootstrapClassLoaderInjector() {
    //
  }

  @Override
  @SneakyThrows
  public void addFile(final @Nullable ClassLoader classLoader, @NotNull File file) {
    if(classLoader != null) {
      throw new IllegalStateException("target class loader is not system class loader");
    }
    JvmHacker.instrumentation().appendToBootstrapClassLoaderSearch(new JarFile(file));
  }

  @Override
  @SneakyThrows
  public void addURL(final @Nullable ClassLoader classLoader, @NotNull URL url) {
    if(classLoader != null) {
      throw new IllegalStateException("target class loader is not bootstrap class loader");
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
      JvmHacker.instrumentation().appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
    } else {
      throw new IllegalStateException("unsupported protocol " + url.getProtocol() + " in " + url);
    }
  }

  @Override
  public boolean isAcceptable(@Nullable ClassLoader classLoader) {
    return classLoader == null;
  }
}
