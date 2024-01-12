package ink.bgp.asteroid.bukkit;

import ink.bgp.asteroid.api.Asteroid;
import ink.bgp.asteroid.api.scope.AsteroidDependency;
import ink.bgp.asteroid.core.injector.JarInjector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

@Singleton
public class AsteroidBukkitCore {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(AsteroidBukkitCore.class);

  private final @NotNull Asteroid asteroid;
  private final @NotNull Yaml pluginYmlYaml;

  private List<AsteroidDependency> pluginLibraryDependencies;
  private Map<String, AsteroidDependency> pluginDependencies;
  private boolean dependenciesInjected;

  @Inject
  private AsteroidBukkitCore(final @NotNull Asteroid asteroid) {
    this.asteroid = asteroid;
    this.pluginYmlYaml = new Yaml(new SafeConstructor(new LoaderOptions()));
  }

  private @Nullable Class<?> onPluginLoaderLoadClass(
      final @NotNull URLClassLoader classLoader,
      final @NotNull String pluginName,
      final @NotNull String className) {
    return null;
  }

  private void onPluginLoaderCreated(final @NotNull URLClassLoader classLoader, final @NotNull String pluginName) {
    if (!dependenciesInjected) {
      if (pluginDependencies.containsKey(pluginName)) {
        for (final AsteroidDependency pluginLibraryDependency : pluginLibraryDependencies) {
          JarInjector.inject(classLoader, pluginLibraryDependency.file());
        }
        logger.info("inject runtime dependencies into {}", pluginName);
      }
      dependenciesInjected = true;
    }
  }

  @SneakyThrows
  private void onLoadPlugins(final @NotNull Object craftServer) {
    final Path pluginsPath = Paths.get("plugins");
    if (!Files.exists(pluginsPath)) {
      Files.createDirectories(pluginsPath);
    }

    final Map<String, File> localPlugins = new LinkedHashMap<>();
    try (final Stream<Path> pluginFiles = Files.list(pluginsPath)) {
      final Iterator<Path> pluginFileIterator = pluginFiles.iterator();
      while (pluginFileIterator.hasNext()) {
        final Path pluginFile = pluginFileIterator.next();
        final String pluginFileName = pluginFile.getFileName().toString();
        if (Files.isDirectory(pluginFile) || !pluginFileName.endsWith(".jar")) {
          continue;
        }
        if (pluginFileName.endsWith(".@asteroid@.jar")) {
          Files.delete(pluginFile);
        } else {
          final String pluginName = loadPluginName(pluginFile.toFile());
          if (pluginName == null) {
            logger.warn("found file in {}, but can't find plugin name", pluginFileName);
          } else {
            localPlugins.put(pluginName, pluginFile.toFile());
          }
        }
      }
    }

    final List<AsteroidDependency> bukkitDependencies = asteroid.scope("bukkit").collectDependencies();
    for (final AsteroidDependency bukkitDependency : bukkitDependencies) {
      JarInjector.inject(craftServer.getClass().getClassLoader(), bukkitDependency.file());
    }

    final List<AsteroidDependency> pluginRuntimeDependencies = asteroid.scope("plugin").collectDependencies();

    final Map<String, AsteroidDependency> pluginDependencies = new LinkedHashMap<>();
    final List<AsteroidDependency> pluginLibraryDependencies = new ArrayList<>();

    for (final AsteroidDependency pluginRuntimeDependency : pluginRuntimeDependencies) {
      final String pluginName = loadPluginName(pluginRuntimeDependency.file());
      if (pluginName == null) {
        pluginLibraryDependencies.add(pluginRuntimeDependency);
      } else {
        final File localPluginSameName = localPlugins.get(pluginName);
        if (localPluginSameName != null) {
          logger.info("found dependency {} is plugin, but have local plugin {} with same name", pluginRuntimeDependency, localPluginSameName);
          continue;
        }
        final AsteroidDependency localPluginDependency = pluginDependencies.get(pluginName);
        if (localPluginDependency != null) {
          logger.info("found dependency {} is plugin, but have been loaded dependency {} with same name", pluginRuntimeDependency, localPluginDependency);
          continue;
        }
        pluginDependencies.put(pluginName, pluginRuntimeDependency);
      }
    }

    for (final AsteroidDependency pluginDependency : pluginDependencies.values()) {
      Files.createSymbolicLink(
          pluginsPath.resolve(safeDependencyName(pluginDependency) + ".@asteroid@.jar"),
          pluginDependency.file().toPath());
    }

    this.pluginLibraryDependencies = Collections.unmodifiableList(pluginLibraryDependencies);
    this.pluginDependencies = Collections.unmodifiableMap(pluginDependencies);
  }

  private @NotNull String safeDependencyName(final @NotNull AsteroidDependency dependency) {
    return dependency.toString()
        .replace(':', '_')
        .replace('@', '_')
        .replace('#', '_');
  }

  @SneakyThrows
  private @Nullable String loadPluginName(final @NotNull File pluginFile) {
    try (final JarFile localPlugin = new JarFile(pluginFile)) {
      final JarEntry entry = localPlugin.getJarEntry("plugin.yml");
      if (entry == null) {
        return null;
      }
      final Map<String, Object> pluginYml;
      try (final InputStream pluginYmlInput = localPlugin.getInputStream(entry)) {
        pluginYml = pluginYmlYaml.load(pluginYmlInput);
      }
      final Object pluginName = pluginYml.get("name");
      return pluginName == null ? null : pluginName.toString();
    }
  }
}
