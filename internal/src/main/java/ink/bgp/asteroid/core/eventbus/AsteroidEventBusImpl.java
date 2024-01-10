package ink.bgp.asteroid.core.eventbus;

import com.google.common.eventbus.EventBus;
import ink.bgp.asteroid.api.asteroid.AsteroidEventBus;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public final class AsteroidEventBusImpl implements AsteroidEventBus {
  private final @NotNull EventBus eventBus;

  @Inject
  private AsteroidEventBusImpl() {
    this.eventBus = new EventBus();
  }

  @Override
  public void register(final @NotNull Object listener) {
    eventBus.register(listener);
  }

  @Override
  public void unregister(final @NotNull Object listener) {
    eventBus.unregister(listener);
  }

  @Override
  public void post(final @NotNull Object event) {
    eventBus.post(event);
  }
}
