package ink.bgp.asteroid.javadowngrader;

import com.google.auto.service.AutoService;
import com.google.inject.Inject;
import ink.bgp.asteroid.api.plugin.AsteroidPlugin;
import ink.bgp.asteroid.core.util.DefineClassUtil;
import lombok.SneakyThrows;
import net.lenni0451.classtransform.TransformerManager;
import net.raphimc.javadowngrader.impl.classtransform.JavaDowngraderTransformer;
import net.raphimc.javadowngrader.util.JavaVersion;
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
            .depCollector(this::injectDepends)
            .build());

    if (System.getProperty("spoofJavaVersion") != null) {
      final JavaVersion spoofedJavaVersion = JavaVersion.getByName(System.getProperty("spoofJavaVersion"));
      if (spoofedJavaVersion == null) {
        System.err.println("Unable to find version '" + System.getProperty("spoofJavaVersion") + "'");
        System.exit(-1);
      }
      System.setProperty("java.version", spoofedJavaVersion.getFakeJavaVersionName());
      System.setProperty("java.class.version", String.valueOf(spoofedJavaVersion.getVersion()));
      System.setProperty("java.specification.version", spoofedJavaVersion.getFakeSpecificationVersionName());
    }

    Message.info("\tJava Downgrader plugin enabled");
    Message.info("\tIf any plugin works wrongly, don't report to the authors");
    Message.info("\tReport to HongYunCloud (hongyuncloud@proton.me) first");
    Message.info("\tYou can also report it on https://github.com/RaphiMC/JavaDowngrader/issues");
    Message.info("");
  }

  @SneakyThrows
  private void injectDepends(final @NotNull String name) {
    try {
      Class.forName(name, false, null);
    } catch (ClassNotFoundException e) {
      try (final InputStream in = JavaDowngraderPlugin.class.getClassLoader().getResourceAsStream(name + ".class")) {
        if (in == null) {
          throw new IllegalStateException("runtime class " + name + " not found");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) baos.write(buf, 0, len);
        byte[] bytes = baos.toByteArray();
        DefineClassUtil.defineClass(name, bytes, 0, bytes.length, null, null);
      } catch (Throwable ex) {
        //
      }
    }
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
