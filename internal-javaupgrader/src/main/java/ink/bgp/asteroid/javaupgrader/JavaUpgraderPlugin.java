package ink.bgp.asteroid.javaupgrader;

import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import ink.bgp.asteroid.api.plugin.AsteroidPlugin;
import ink.bgp.asteroid.javaupgrader.transform.UnsafeRedirectTransformer;
import lombok.SneakyThrows;
import net.lenni0451.classtransform.TransformerManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaUpgraderPlugin implements AsteroidPlugin {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(JavaUpgraderPlugin.class);

  private final @NotNull UnsafeRedirectTransformer unsafeRedirectTransformer;
  private final @NotNull TransformerManager transformerManager;

  @Inject
  private JavaUpgraderPlugin(
      final @NotNull UnsafeRedirectTransformer unsafeRedirectTransformer,
      final @NotNull TransformerManager transformerManager) {
    this.unsafeRedirectTransformer = unsafeRedirectTransformer;
    this.transformerManager = transformerManager;
  }

  @Override
  @SneakyThrows
  public void load() {
    unsafeRedirectTransformer.load();
    transformerManager.addBytecodeTransformer(unsafeRedirectTransformer);
  }

  @AutoService(AsteroidPlugin.Metadata.class)
  public static final class Metadata implements AsteroidPlugin.Metadata {
    @Override
    public @NotNull String name() {
      return "asteroid-java-upgrader";
    }

    @Override
    public @NotNull String @NotNull [] authors() {
      return new String[]{"红云cloud"};
    }

    @Override
    public @NotNull String description() {
      return "Asteroid plugin which can upgrade Java classes/programs up to Java 17";
    }

    @Override
    public @NotNull Class<? extends AsteroidPlugin> pluginClass() {
      return JavaUpgraderPlugin.class;
    }
  }
}
