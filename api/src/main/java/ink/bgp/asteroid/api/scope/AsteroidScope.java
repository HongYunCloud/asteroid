package ink.bgp.asteroid.api.scope;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public interface AsteroidScope {
  void addDependency(
      final @NotNull String configuration,
      final @NotNull String group,
      final @NotNull String module,
      final @NotNull String version,
      final @Nullable String targetConfiguration);

  void run();

  @NotNull List<@NotNull AsteroidDependency> collectDependencies(final @NotNull String configuration);

  @Nullable AsteroidDependency getDependency(
      final @NotNull String configuration,
      final @NotNull String group,
      final @NotNull String module,
      final @NotNull String version,
      final @Nullable String targetConfiguration);
}
