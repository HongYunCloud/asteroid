package ink.bgp.asteroid.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import ink.bgp.asteroid.api.Asteroid;
import ink.bgp.asteroid.api.plugin.AsteroidPlugin;
import ink.bgp.asteroid.api.scope.AsteroidDependency;
import ink.bgp.asteroid.api.scope.AsteroidScope;
import ink.bgp.asteroid.core.injector.JarInjector;
import ink.bgp.asteroid.core.log.ClasstransformLogBridge;
import ink.bgp.asteroid.core.log.IvyLogBridge;
import ink.bgp.asteroid.core.plugin.AsteroidModule;
import ink.bgp.asteroid.core.scope.AsteroidScopeImpl;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.ivy.util.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

public final class AsteroidCore implements Asteroid {
  private final @NotNull Path dataDirectory = Paths.get("asteroid");
  private final @NotNull EventBus eventBus = new EventBus();

  @Getter(lazy = true)
  private final @NotNull Instrumentation instrumentation = loadInstrumentation();

  private final @NotNull Map<String, AsteroidScope> scopeMap = Collections.synchronizedMap(new HashMap<>());

  private @Nullable Injector injector;
  private @NotNull Map<String, AsteroidPlugin> pluginMap = Collections.synchronizedMap(new HashMap<>());

  public AsteroidCore() {
    Asteroid.$set$instance(this);
    configureLogs();
  }

  private void configureLogs() {
    net.lenni0451.classtransform.utils.log.Logger.LOGGER = new ClasstransformLogBridge(
        LoggerFactory.getLogger("net.lenni0451.classtransform"));
    Message.setDefaultLogger(new IvyLogBridge(
        LoggerFactory.getLogger("org.apache.ivy")));
  }

  @SneakyThrows
  private @NotNull Instrumentation loadInstrumentation() {
    return  (Instrumentation) Class.forName("ink.bgp.asteroid.loader.AsteroidMain")
        .getMethod("instrumentation")
        .invoke(null);
  }

  public @NotNull Injector injector() {
    if (injector == null) {
      throw new IllegalStateException("asteroid core is still not loaded");
    }
    return injector;
  }

  @Override
  public void injectMembers(final @NotNull Object instance) {
    injector().injectMembers(instance);
  }

  @Override
  public <T> Supplier<T> getProvider(final @NotNull Class<T> type) {
    final Provider<T> provider = injector().getProvider(type);
    return provider == null ? null : provider::get;
  }

  @Override
  public <T> T getInstance(final @NotNull Class<T> type) {
    return injector().getInstance(type);
  }

  @Override
  public @NotNull AsteroidScope scope(final @NotNull String scopeName) {
    return scopeMap.computeIfAbsent(scopeName, it ->
        new AsteroidScopeImpl(scopeName));
  }

  @Override
  public void run() {
    scope("asteroid").run();
    injectClassPath(AsteroidCore.class.getClassLoader(), "asteroid");

    loadAsteroidPlugin();

    scope("").run();
    injectClassPath(ClassLoader.getSystemClassLoader());
  }

  private void loadAsteroidPlugin() {
    if (this.injector != null) {
      throw new IllegalStateException("asteroid core have been loaded");
    }
    final ServiceLoader<AsteroidModule> moduleLoader = ServiceLoader.load(
        AsteroidModule.class,
        AsteroidCore.class.getClassLoader());
    final List<Module> moduleList = new ArrayList<>();
    for (final AsteroidModule module : moduleLoader) {
      moduleList.add(module);
    }

    this.injector = Guice.createInjector(moduleList);

    final ServiceLoader<AsteroidPlugin.Metadata> metadataLoader = ServiceLoader.load(
        AsteroidPlugin.Metadata.class,
        AsteroidCore.class.getClassLoader());

    for (final AsteroidPlugin.Metadata metadata : metadataLoader) {
      pluginMap.put(metadata.name(), injector().getInstance(metadata.pluginClass()));
    }

    for (final Map.Entry<String, AsteroidPlugin> entry : pluginMap.entrySet()) {
      Message.info(":: loading asteroid plugin :: name = " + entry.getKey());
      entry.getValue().load();
    }
  }

  @Override
  public void injectClassPath(
      final @Nullable ClassLoader classLoader,
      final @Nullable String scope,
      final @Nullable String configuration) {
    for (final AsteroidDependency dependency : scope(scope == null ? "" : scope)
        .collectDependencies(configuration == null ? "runtime" : configuration)) {
      if (dependency.file().getName().endsWith(".jar")) {
        JarInjector.inject(classLoader, dependency.file());
      }
    }
  }
}
