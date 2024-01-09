package ink.bgp.asteroid.api;

import ink.bgp.asteroid.api.scope.AsteroidScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Asteroid {
  @NotNull AsteroidScope scope(final @NotNull String scopeName);

  void run();

  default void injectClassPath(final @Nullable ClassLoader classLoader) {
    injectClassPath(classLoader, null, null);
  }

  default void injectClassPath(
      final @Nullable ClassLoader classLoader,
      final @Nullable String scope) {
    injectClassPath(classLoader, scope, null);
  }

  void injectClassPath(
      final @Nullable ClassLoader classLoader,
      final @Nullable String scope,
      final @Nullable String configuration);

  static @NotNull Asteroid instance() {
    return AsteroidHolder.instance();
  }

  static void $set$instance(final @NotNull Asteroid instance) {
    AsteroidHolder.setInstance(instance);
  }
}
