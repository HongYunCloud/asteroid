package ink.bgp.asteroid.bukkit;

import com.google.auto.service.AutoService;
import ink.bgp.asteroid.api.Asteroid;
import ink.bgp.asteroid.api.asteroid.AsteroidEventBus;
import ink.bgp.asteroid.api.plugin.AsteroidPlugin;
import ink.bgp.asteroid.bukkit.transform.CraftServerLoadPluginsTransform;
import ink.bgp.asteroid.bukkit.transform.PluginClassLoaderTransform;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.lenni0451.classtransform.TransformerManager;
import org.jetbrains.annotations.NotNull;

@Singleton
public final class AsteroidBukkitPlugin implements AsteroidPlugin {
  private final @NotNull Asteroid asteroid;
  private final @NotNull TransformerManager transformerManager;
  private final @NotNull AsteroidEventBus eventBus;
  private final @NotNull CraftServerLoadPluginsTransform craftServerLoadPluginsTransform;
  private final @NotNull PluginClassLoaderTransform pluginClassLoaderTransform;

  @Inject
  private AsteroidBukkitPlugin(
      final @NotNull Asteroid asteroid,
      final @NotNull TransformerManager transformerManager,
      final @NotNull AsteroidEventBus eventBus,
      final @NotNull CraftServerLoadPluginsTransform craftServerLoadPluginsTransform,
      final @NotNull PluginClassLoaderTransform pluginClassLoaderTransform) {
    this.asteroid = asteroid;
    this.transformerManager = transformerManager;
    this.eventBus = eventBus;
    this.craftServerLoadPluginsTransform = craftServerLoadPluginsTransform;
    this.pluginClassLoaderTransform = pluginClassLoaderTransform;
  }

  @Override
  public void load() {
    eventBus.register(this);

    pluginClassLoaderTransform.load();
    transformerManager.addRawTransformer("org.bukkit.plugin.java.PluginClassLoader", pluginClassLoaderTransform);

    transformerManager.addRawTransformer("org.bukkit.craftbukkit.v1_20_R2.CraftServer", craftServerLoadPluginsTransform);
    transformerManager.addRawTransformer("org.bukkit.craftbukkit.v1_20_R3.CraftServer", craftServerLoadPluginsTransform);

    asteroid.scope("bukkit").run();
    asteroid.scope("plugin").run();
  }

  @AutoService(AsteroidPlugin.Metadata.class)
  public static final class Metadata implements AsteroidPlugin.Metadata {
    @Override
    public @NotNull String name() {
      return "asteroid-bukkit";
    }

    @Override
    public @NotNull String @NotNull [] authors() {
      return new String[]{"红云cloud"};
    }

    @Override
    public @NotNull String description() {
      return "Asteroid with bukkit";
    }

    @Override
    public @NotNull Class<? extends AsteroidPlugin> pluginClass() {
      return AsteroidBukkitPlugin.class;
    }
  }
}
