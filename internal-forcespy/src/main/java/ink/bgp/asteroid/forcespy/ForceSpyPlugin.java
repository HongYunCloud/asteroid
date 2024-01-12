package ink.bgp.asteroid.forcespy;

import com.google.auto.service.AutoService;
import ink.bgp.asteroid.api.plugin.AsteroidPlugin;
import ink.bgp.asteroid.forcespy.transform.ForceSpyTransform;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import net.lenni0451.classtransform.TransformerManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ForceSpyPlugin implements AsteroidPlugin {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(ForceSpyPlugin.class);

  private final @NotNull ForceSpyTransform forceSpyTransform;
  private final @NotNull TransformerManager transformerManager;

  @Inject
  private ForceSpyPlugin(
      final @NotNull ForceSpyTransform forceSpyTransform,
      final @NotNull TransformerManager transformerManager) {
    this.forceSpyTransform = forceSpyTransform;
    this.transformerManager = transformerManager;
  }

  @Override
  @SneakyThrows
  public void load() {
    transformerManager.addBytecodeTransformer(forceSpyTransform);
  }

  @AutoService(AsteroidPlugin.Metadata.class)
  public static final class Metadata implements AsteroidPlugin.Metadata {
    @Override
    public @NotNull String name() {
      return "asteroid-forcespy";
    }

    @Override
    public @NotNull String @NotNull [] authors() {
      return new String[]{"红云cloud"};
    }

    @Override
    public @NotNull String description() {
      return "Asteroid plugin which can force all classloader use spy";
    }

    @Override
    public @NotNull Class<? extends AsteroidPlugin> pluginClass() {
      return ForceSpyPlugin.class;
    }
  }
}
