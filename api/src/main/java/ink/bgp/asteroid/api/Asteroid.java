package ink.bgp.asteroid.api;

import ink.bgp.asteroid.api.scope.AsteroidScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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

  static <T> T get(final @NotNull Class<T> clazz) {
    return instance().getInstance(clazz);
  }

  static @NotNull Asteroid instance() {
    final Asteroid instance = $Holder.instance;
    if (instance == null) {
      throw new IllegalStateException("Asteroid is not initialized");
    }
    return instance;
  }

  static void $set$instance(final @NotNull Asteroid instance) {
    $Holder.instance = instance;
  }

  final class $Holder {
    private static @UnknownNullability Asteroid instance = null;

    private $Holder() {
      throw new UnsupportedOperationException();
    }
  }
}
