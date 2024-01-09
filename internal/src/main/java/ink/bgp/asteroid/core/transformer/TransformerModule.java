package ink.bgp.asteroid.core.transformer;

import com.google.auto.service.AutoService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import ink.bgp.asteroid.core.plugin.AsteroidModule;
import jakarta.inject.Singleton;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import org.jetbrains.annotations.NotNull;

@AutoService(AsteroidModule.class)
public class TransformerModule extends AbstractModule implements AsteroidModule {
  @Provides
  @Singleton
  public @NotNull MutableDelegateClassProvider basicClassProvider() {
    return new MutableDelegateClassProvider(
        new BasicClassProvider(ClassLoader.getSystemClassLoader()));
  }

  @Provides
  @Singleton
  public @NotNull TransformerManager transformerManager(final @NotNull BasicClassProvider basicClassProvider) {
    return new TransformerManager(basicClassProvider);
  }
}
