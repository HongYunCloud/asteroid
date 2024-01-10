package ink.bgp.asteroid.core;

import com.google.auto.service.AutoService;
import com.google.inject.AbstractModule;
import ink.bgp.asteroid.api.Asteroid;
import ink.bgp.asteroid.api.asteroid.AsteroidEventBus;
import ink.bgp.asteroid.core.eventbus.AsteroidEventBusImpl;
import ink.bgp.asteroid.core.plugin.AsteroidModule;
import ink.bgp.asteroid.core.spy.AsteroidSpyServiceImpl;
import ink.bgp.asteroid.core.transformer.TransformPreHandler;
import net.lenni0451.classtransform.TransformerManager;

import java.lang.instrument.Instrumentation;

@AutoService(AsteroidModule.class)
public class AsteroidCoreModule extends AbstractModule implements AsteroidModule {
  @Override
  protected void configure() {
    AsteroidCore core = (AsteroidCore) Asteroid.instance();

    bind(AsteroidCore.class).toInstance(core);
    bind(Instrumentation.class).toInstance(core.instrumentation());

    bind(TransformPreHandler.class);
    bind(TransformerManager.class).toProvider(TransformPreHandler.class);

    bind(AsteroidEventBus.class).to(AsteroidEventBusImpl.class);

    bind(AsteroidSpyServiceImpl.class);
  }
}
