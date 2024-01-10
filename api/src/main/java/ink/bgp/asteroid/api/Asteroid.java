package ink.bgp.asteroid.api;

import ink.bgp.asteroid.api.scope.AsteroidScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface Asteroid {
  void injectMembers(@NotNull Object instance);

  <T> Supplier<T> getProvider(@NotNull Class<T> type);

  <T> T getInstance(@NotNull Class<T> type);

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
