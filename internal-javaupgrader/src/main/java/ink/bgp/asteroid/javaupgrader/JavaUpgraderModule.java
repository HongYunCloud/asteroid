package ink.bgp.asteroid.javaupgrader;

import com.google.auto.service.AutoService;
import com.google.inject.AbstractModule;
import ink.bgp.asteroid.core.plugin.AsteroidModule;
import ink.bgp.asteroid.javaupgrader.transform.UnsafeRedirectTransformer;

@AutoService(AsteroidModule.class)
public class JavaUpgraderModule extends AbstractModule implements AsteroidModule {
  @Override
  protected void configure() {
    bind(UnsafeRedirectTransformer.class);
  }
}
