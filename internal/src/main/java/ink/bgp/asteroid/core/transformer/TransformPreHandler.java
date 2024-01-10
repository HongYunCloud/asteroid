package ink.bgp.asteroid.core.transformer;

import com.google.common.collect.MapMaker;
import ink.bgp.asteroid.core.util.ClassNameUtil;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.utils.log.Logger;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Singleton
public class TransformPreHandler implements ClassFileTransformer, IClassProvider,
    Provider<@NotNull TransformerManager> {
  private final @NotNull Map<@NotNull ClassLoader, @NotNull IClassProvider> providers = new MapMaker()
      .weakKeys()
      .weakValues()
      .makeMap();
  private final @NotNull ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

  private @Nullable TransformerManager transformerManager;

  @Inject
  private TransformPreHandler() {
    appendClassLoader(new ClassLoader(null) {});
  }

  public void loadTransformerManager(final @NotNull TransformerManager transformerManager) {
    if(this.transformerManager != null) {
      throw new IllegalStateException("transform pre handler have been bind");
    }
    this.transformerManager = transformerManager;
  }

  @Override
  public byte @NotNull [] getClass(final @NotNull String name) throws ClassNotFoundException {
    rwLock.readLock().lock();
    try {
      for (final IClassProvider delegate : providers.values()) {
        try {
          return delegate.getClass(name);
        } catch (ClassNotFoundException ignored) {
          //
        }
      }
    } finally {
      rwLock.readLock().unlock();
    }
    throw new ClassNotFoundException(name);
  }

  @Override
  public @NotNull Map<@NotNull String, @NotNull Supplier<byte @NotNull []>> getAllClasses() {
    final Map<String, Supplier<byte[]>> classes = new HashMap<>();

    rwLock.readLock().lock();
    try {
      for (final IClassProvider delegate : providers.values()) {
        classes.putAll(delegate.getAllClasses());
      }
    } finally {
      rwLock.readLock().unlock();
    }

    return classes;
  }

  public void appendClassLoader(@NotNull ClassLoader loader) {
    rwLock.writeLock().lock();
    try {
      providers.put(loader, new BasicClassProvider(loader));
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  @Override
  public byte @Nullable [] transform(@Nullable ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    if (className == null) {
      try {
        className = ClassNameUtil.getClassName(classfileBuffer);
      } catch (Throwable e) {
        Logger.error("Failed to get class name from bytes \n{}",
            Base64.getEncoder().encode(classfileBuffer), e);
      }
    }

    final boolean containsClassLoader;
    rwLock.readLock().lock();
    try {
      containsClassLoader = providers.containsKey(loader);
    } finally {
      rwLock.readLock().unlock();
    }

    if(loader != null && !containsClassLoader) {
      appendClassLoader(loader);
    }

    if (transformerManager != null) {
      return transformerManager.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    } else {
      return null;
    }
  }

  @Override
  public @NotNull TransformerManager get() {
    return new TransformerManager(this);
  }
}
