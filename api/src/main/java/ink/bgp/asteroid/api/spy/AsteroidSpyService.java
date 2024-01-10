package ink.bgp.asteroid.api.spy;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

public interface AsteroidSpyService {
  @NotNull CallSite getHandle(@NotNull String name);

  @NotNull String saveConstantHandle(@NotNull MethodHandle methodHandle);

  @NotNull String saveMutableHandle(@NotNull MethodHandle methodHandle);

  void setMutableHandle(@NotNull String name, @NotNull MethodHandle methodHandle);
}
