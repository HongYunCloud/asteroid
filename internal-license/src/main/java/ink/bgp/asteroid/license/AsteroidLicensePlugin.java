package ink.bgp.asteroid.license;

import com.google.auto.service.AutoService;
import ink.bgp.asteroid.api.plugin.AsteroidPlugin;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.ivy.util.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
public final class AsteroidLicensePlugin implements AsteroidPlugin {
  @Inject
  private AsteroidLicensePlugin() {
    //
  }

  @Override
  public void load() {
    Message.info("\tAsteroid license plugin enabled");
    Message.info("\tASTEROID 授权验证插件已启用");
    Message.info("");
    Message.info("\t欢迎加群: 708429599");
    Message.info("");

    final Random random = ThreadLocalRandom.current();
    final long finishedTime = System.currentTimeMillis() + random.nextInt(1000, 3000);
    final double[] doubles = new double[8192];
    for (int i = 0; i < doubles.length; i++) {
      doubles[i] = random.nextDouble();
    }
    do {
      for (int i = 1; i < doubles.length; i++) {
        for (int j = 0; j < doubles.length; j++) {
          doubles[i] *= doubles[i-1];
        }
      }
    } while (System.currentTimeMillis() < finishedTime);

    final String plan = System.getProperty("asteroid.plan", "free");
    if(!"enterprise".equals(plan)) {
      Message.info("Asteroid license 验证失败");
      System.exit(-1);
    }
  }

  @AutoService(AsteroidPlugin.Metadata.class)
  public static final class Metadata implements AsteroidPlugin.Metadata {
    @Override
    public @NotNull String name() {
      return "asteroid-license";
    }

    @Override
    public @NotNull String @NotNull [] authors() {
      return new String[] { "红云cloud" };
    }

    @Override
    public @NotNull String description() {
      return "Asteroid License Plugin";
    }

    @Override
    public @NotNull Class<? extends AsteroidPlugin> pluginClass() {
      return AsteroidLicensePlugin.class;
    }
  }
}
