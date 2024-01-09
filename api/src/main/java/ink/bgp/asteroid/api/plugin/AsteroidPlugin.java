package ink.bgp.asteroid.api.plugin;

import org.jetbrains.annotations.NotNull;

public interface AsteroidPlugin {
  void load();

  interface Metadata {
    @NotNull String name();
    @NotNull String @NotNull [] authors();
    @NotNull String description();

    @NotNull Class<? extends AsteroidPlugin> pluginClass();
  }
}
