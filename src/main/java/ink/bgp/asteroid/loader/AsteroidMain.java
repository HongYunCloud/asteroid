package ink.bgp.asteroid.loader;

import ink.bgp.asteroid.api.Asteroid;
import ink.bgp.asteroid.loader.archive.Archive;
import ink.bgp.asteroid.loader.archive.ExplodedArchive;
import ink.bgp.asteroid.loader.archive.JarFileArchive;
import ink.bgp.asteroid.loader.jar.Handler;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public final class AsteroidMain {
  public static final @NotNull String ASTEROID_KEY = "ink.bgp.asteroid";
  public static final boolean EXPOSE_JAR_IN_JAR = Boolean.getBoolean(ASTEROID_KEY + ".expose");

  private static final @NotNull String ASTEROID_LAUNCHER_MAIN_CLASS = "ink.bgp.asteroid.core.AsteroidCoreLauncher";

  static {
    final Console systemConsole = System.console();
    final String message = "asteroid package tool 1.0-SNAPSHOT deploying";
    if(systemConsole != null){
      LogoPrinter.print(systemConsole.writer(), message);
    } else {
      LogoPrinter.print(System.out, message);
    }

    if(EXPOSE_JAR_IN_JAR) {
      ink.bgp.asteroid.loader.jar.JarFile.registerUrlProtocolHandler();
    }
  }

  private static final @NotNull AtomicReference<@Nullable Object> instrumentation = new AtomicReference<>();

  private AsteroidMain() {
    throw new UnsupportedOperationException();
  }

  @SneakyThrows
  public static @NotNull Instrumentation instrumentation() {
    Object value = instrumentation.get();
    if (value == null) {
      synchronized(instrumentation) {
        value = instrumentation.get();
        if (value == null) {
          try {
            value = JvmHacker.instrumentation();
          }catch (Exception e) {
            value = e;
          }
          instrumentation.set(value);
        }
      }
    }
    if (value instanceof Throwable) {
      throw (Throwable) value;
    } else {
      return (Instrumentation) value;
    }
  }

  private static @NotNull Archive createArchive() throws Exception {
    ProtectionDomain protectionDomain = AsteroidMain.class.getProtectionDomain();
    CodeSource codeSource = protectionDomain.getCodeSource();
    URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
    String path = (location != null) ? location.getSchemeSpecificPart() : null;
    if (path == null) {
      throw new IllegalStateException("Unable to determine code source archive");
    }
    File root = new File(path);
    if (!root.exists()) {
      throw new IllegalStateException("Unable to determine code source archive from " + root);
    }
    return (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
  }

  private static @NotNull List<@NotNull URL> scanNestedArchives(final @NotNull Archive rootArchive) throws IOException {
    final List<URL> result = new ArrayList<>();
    final Iterator<Archive> nestedArchives = rootArchive.getNestedArchives(entry ->
      (entry.isDirectory() && "ASTEROID-LIBS".equals(entry.getName()))
          || entry.getName().startsWith("ASTEROID-LIBS/"), null);
    while (nestedArchives.hasNext()) {
      result.add(nestedArchives.next().getUrl());
    }
    return result;
  }

  public static void premain(final @NotNull String agentArgs, final @NotNull Instrumentation inst) throws Exception {
    instrumentation.set(inst);
    main(new String[0]);
  }

  public static void agentmain(final @NotNull String agentArgs, final @NotNull Instrumentation inst) {
    instrumentation.set(inst);
  }

  public static void main(final @NotNull String @NotNull [] args) throws Exception {
    final Instrumentation instrumentation = instrumentation();

    final Archive rootArchive = createArchive();
    final LaunchedURLClassLoader launchedClassLoader = new LaunchedURLClassLoader(
        EXPOSE_JAR_IN_JAR,
        rootArchive,
        scanNestedArchives(rootArchive).toArray(new URL[0]),
        AsteroidMain.class.getClassLoader());

    final Runnable mainRunnable = (Runnable) Class.forName(ASTEROID_LAUNCHER_MAIN_CLASS, false, launchedClassLoader)
        .getConstructor()
        .newInstance();
    mainRunnable.run();

    String targetMainClass = null;
    if (args.length > 0) {
      final String nextName = args[0];

      if(nextName.endsWith(".jar")) {
        try(final JarFile jarFile = new JarFile(nextName)) {
          targetMainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
          instrumentation.appendToSystemClassLoaderSearch(jarFile);
        }
      } else if (nextName.contains(":")) {
        final String[] parts = nextName.split(":");
        Asteroid.instance().addDependency("system", parts[0], parts[1], parts[2], parts.length > 3 ? parts[3] : null);
        targetMainClass = nextName;
      } else {
        targetMainClass = nextName;
      }
    }

    Asteroid.instance().run();
    Asteroid.instance().injectSystemClassPath();

    if (targetMainClass != null) {
      if (targetMainClass.contains(":")) {
        final String[] parts = targetMainClass.split(":");
        final File targetFile = Asteroid.instance()
            .getFile("system", parts[0], parts[1], parts[2], parts.length > 3 ? parts[3] : null);
        try(final JarFile jarFile = new JarFile(targetFile)) {
          targetMainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
        }
      }

      final String[] nextArgs = new String[args.length - 1];
      System.arraycopy(args, 1, nextArgs, 0, nextArgs.length);
      Class.forName(targetMainClass)
          .getMethod("main", String[].class)
          .invoke(null, (Object) nextArgs);
    }
  }

  private static @NotNull JarFile provideNextJarFile(final @NotNull String nextJarName) throws IOException {
    if(nextJarName.endsWith(".jar")) {
      return new JarFile(nextJarName);
    } else if (nextJarName.contains(":")) {
      throw new UnsupportedOperationException("not support remote dependency as boot jar yet");
    } else {
      return new JarFile(nextJarName);
    }
  }
}
