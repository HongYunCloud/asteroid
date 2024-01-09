package ink.bgp.asteroid.core.injector;

import ink.bgp.asteroid.api.classloader.AccessibleUrlPath;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class JarInjector {
  private JarInjector() {
    throw new UnsupportedOperationException();
  }

  @SneakyThrows
  public static void inject(final @Nullable ClassLoader classLoader, final @NotNull File jarFile) {
    if (classLoader instanceof AccessibleUrlPath) {
      AccessibleInjector.INSTANCE.addFile(classLoader, jarFile);
    } else if (classLoader instanceof URLClassLoader) {
      UrlClassLoaderInjector.INSTANCE.addFile((URLClassLoader) classLoader, jarFile);
    } else if(classLoader == null) {
      BootstrapClassLoaderInjector.INSTANCE.addFile(null, jarFile);
    } else if (SystemClassLoaderInjector.INSTANCE.isAcceptable(classLoader)) {
      SystemClassLoaderInjector.INSTANCE.addFile(classLoader, jarFile);
    } else {
      throw new UnsupportedOperationException("unsupported inject for classloader " + classLoader.getClass());
    }
  }

  @SneakyThrows
  public static void inject(final @Nullable ClassLoader classLoader, final @NotNull URL url) {
    if (classLoader instanceof AccessibleUrlPath) {
      AccessibleInjector.INSTANCE.addURL(classLoader, url);
    } else if (classLoader instanceof URLClassLoader) {
      UrlClassLoaderInjector.INSTANCE.addURL((URLClassLoader) classLoader, url);
    } else if(classLoader == null) {
      BootstrapClassLoaderInjector.INSTANCE.addURL(null, url);
    } else if (SystemClassLoaderInjector.INSTANCE.isAcceptable(classLoader)) {
      SystemClassLoaderInjector.INSTANCE.addURL(classLoader, url);
    } else {
      throw new UnsupportedOperationException("unsupported inject for classloader " + classLoader.getClass());
    }
  }
}
