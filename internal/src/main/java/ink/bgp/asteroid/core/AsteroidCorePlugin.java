package ink.bgp.asteroid.core;

import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import ink.bgp.asteroid.api.plugin.AsteroidPlugin;
import ink.bgp.asteroid.core.transformer.FixClassNameTransform;
import net.lenni0451.classtransform.TransformerManager;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.Instrumentation;

public final class AsteroidCorePlugin implements AsteroidPlugin {
  private final @NotNull Instrumentation instrumentation;
  private final @NotNull TransformerManager transformerManager;

  @Inject
  private AsteroidCorePlugin(final @NotNull Instrumentation instrumentation, final @NotNull TransformerManager transformerManager) {
    this.instrumentation = instrumentation;
    this.transformerManager = transformerManager;
  }

  @Override
  public void load() {
    transformerManager.hookInstrumentation(instrumentation);
    instrumentation.removeTransformer(transformerManager);
    instrumentation.addTransformer(new FixClassNameTransform(transformerManager), instrumentation.isRetransformClassesSupported());
  }

  @AutoService(AsteroidPlugin.Metadata.class)
  public static final class Metadata implements AsteroidPlugin.Metadata {
    @Override
    public @NotNull String name() {
      return "asteroid-core";
    }

    @Override
    public @NotNull String @NotNull [] authors() {
      return new String[] { "红云cloud" };
    }

    @Override
    public @NotNull String description() {
      return "Asteroid Core Plugin";
    }

    @Override
    public @NotNull Class<? extends AsteroidPlugin> pluginClass() {
      return AsteroidCorePlugin.class;
    }
  }
}
