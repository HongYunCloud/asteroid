package ink.bgp.asteroid.javadowngrader;

import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import ink.bgp.asteroid.api.plugin.AsteroidPlugin;
import ink.bgp.asteroid.core.util.DefineClassUtil;
import lombok.SneakyThrows;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.utils.ASMUtils;
import net.raphimc.javadowngrader.impl.classtransform.JavaDowngraderTransformer;
import net.raphimc.javadowngrader.util.ASMUtil;
import org.apache.ivy.util.Message;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class JavaDowngraderPlugin implements AsteroidPlugin {
  private static final @NotNull Logger logger = LoggerFactory.getLogger(JavaDowngraderPlugin.class);

  private final @NotNull TransformerManager transformerManager;

  @Inject
  private JavaDowngraderPlugin(final @NotNull TransformerManager transformerManager) {
    this.transformerManager = transformerManager;
  }

  @Override
  public void load() {
    transformerManager.addBytecodeTransformer(
        JavaDowngraderTransformer.builder(transformerManager)
            .build());
    Message.info("\tJava Downgrader plugin enabled");
    Message.info("\tIf any plugin works wrongly, don't report to the authors");
    Message.info("\tReport to HongYunCloud (hongyuncloud@proton.me) first");
    Message.info("\tYou can also report it on https://github.com/RaphiMC/JavaDowngrader/issues");
    Message.info("");
  }

  @AutoService(AsteroidPlugin.Metadata.class)
  public static final class Metadata implements AsteroidPlugin.Metadata {

    @Override
    public @NotNull String name() {
      return "asteroid-java-downgrader";
    }

    @Override
    public @NotNull String @NotNull [] authors() {
      return new String[] { "红云cloud", "RaphiMC" };
    }

    @Override
    public @NotNull String description() {
      return "Asteroid plugin which can downgrade Java classes/programs down to Java 8";
    }

    @Override
    public @NotNull Class<? extends AsteroidPlugin> pluginClass() {
      return JavaDowngraderPlugin.class;
    }
  }
}
