package ink.bgp.asteroid.api.asteroid;

import org.jetbrains.annotations.NotNull;

public interface AsteroidEventBus {
  void register(@NotNull Object listener);

  void unregister(@NotNull Object listener);

  void post(@NotNull Object event);
}
