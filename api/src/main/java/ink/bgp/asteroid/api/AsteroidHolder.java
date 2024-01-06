package ink.bgp.asteroid.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/* package-private */ final class AsteroidHolder {
  private static @UnknownNullability Asteroid instance = null;
  private AsteroidHolder() {
    throw new UnsupportedOperationException();
  }

  public static @NotNull Asteroid instance() {
    Asteroid instance = AsteroidHolder.instance;
    if (instance == null) {
      throw new IllegalStateException("Asteroid is not initialized");
    }
    return instance;
  }

  public static synchronized void setInstance(@NotNull Asteroid instance) {
    if (AsteroidHolder.instance == null) {
      AsteroidHolder.instance = instance;
    } else {
      throw new IllegalStateException("Asteroid is already initialized");
    }
  }
}
