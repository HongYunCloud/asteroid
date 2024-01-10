package ink.bgp.asteroid.core.spy;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class $AsteroidSpy {
  private static volatile MethodHandle $handle;

  public static void $setHandle(final MethodHandle handle) {
    synchronized ($AsteroidSpy.class) {
      if ($handle != null) {
        throw new IllegalStateException("asteroid spy have been initialized");
      }
      $handle = handle;
    }
  }

  public static MethodHandle $getHandle() {
    final MethodHandle localHandle = $handle;
    if(localHandle != null) {
      return localHandle;
    } else {
      throw new IllegalStateException("asteroid spy still not been initialized");
    }
  }

  public static CallSite $load(final String name) throws Throwable {
    return (CallSite) $getHandle().invokeExact(name);
  }

  public static CallSite $bootstrap(
      final MethodHandles.Lookup caller,
      final String methodName,
      final MethodType methodType) throws Throwable {
    return $load(methodName);
  }
}
