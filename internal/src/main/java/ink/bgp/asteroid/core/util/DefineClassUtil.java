package ink.bgp.asteroid.core.util;

import bot.inker.acj.JvmHacker;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;
import java.util.Objects;

public class DefineClassUtil {
  private static final @NotNull MethodHandle defineClassHandle = findDefineClassHandle();

  private DefineClassUtil() {
    throw new UnsupportedOperationException();
  }

  @SneakyThrows
  private static @NotNull MethodHandle findDefineClassHandle() {
    try {
      Class<?> jdkUnsafeClass = Class.forName("jdk.internal.misc.Unsafe", false, null);
      Object jdkUnsafe = JvmHacker.lookup()
          .findStaticGetter(jdkUnsafeClass, "theUnsafe", jdkUnsafeClass)
          .invoke();
      return JvmHacker.lookup()
          .findVirtual(jdkUnsafeClass, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class))
          .bindTo(jdkUnsafe);
    } catch (ClassNotFoundException | NoSuchMethodException e){
      Class<?> sunUnsafeClass = Class.forName("sun.misc.Unsafe", false, null);
      return JvmHacker.lookup()
          .findVirtual(sunUnsafeClass, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class))
          .bindTo(JvmHacker.unsafe());
    }
  }

  @SneakyThrows
  public static @NotNull Class<?> defineClass(
      final @NotNull String name,
      final byte @NotNull [] b,
      final int off,
      final int len,
      final @Nullable ClassLoader classLoader,
      final @Nullable ProtectionDomain protectionDomain) {
    return (Class<?>) defineClassHandle.invokeExact(name, b, off, len, classLoader, protectionDomain);
  }
}
