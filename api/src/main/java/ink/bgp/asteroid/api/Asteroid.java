package ink.bgp.asteroid.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface Asteroid {
  void run();

  void injectSystemClassPath();

  static @NotNull Asteroid instance() {
    return AsteroidHolder.instance();
  }

  static void $set$instance(final @NotNull Asteroid instance) {
    AsteroidHolder.setInstance(instance);
  }

  void addDependency(
      final @NotNull String configuration,
      final @NotNull String group,
      final @NotNull String module,
      final @NotNull String version,
      final @Nullable String targetConfiguration);

  @Nullable File getFile(
      final @NotNull String configuration,
      final @NotNull String group,
      final @NotNull String module,
      final @NotNull String version,
      final @Nullable String targetConfiguration);
}
