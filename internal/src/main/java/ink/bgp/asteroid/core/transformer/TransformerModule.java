package ink.bgp.asteroid.core.transformer;

import com.google.auto.service.AutoService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import ink.bgp.asteroid.core.plugin.AsteroidModule;
import jakarta.inject.Singleton;
import net.lenni0451.classtransform.TransformerManager;
import org.jetbrains.annotations.NotNull;

@AutoService(AsteroidModule.class)
public class TransformerModule extends AbstractModule implements AsteroidModule {
  @Override
  protected void configure() {
    bind(TransformPreHandler.class);
  }

  @Provides
  @Singleton
  public @NotNull TransformerManager transformerManager(final @NotNull TransformPreHandler transformPreHandler) {
    return new TransformerManager(transformPreHandler);
  }
}
