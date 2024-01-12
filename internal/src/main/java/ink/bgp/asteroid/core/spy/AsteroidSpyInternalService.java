package ink.bgp.asteroid.core.spy;

import ink.bgp.asteroid.api.spy.AsteroidSpyService;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.lang.invoke.MethodHandle;

public interface AsteroidSpyInternalService extends AsteroidSpyService {
  @NotNull InvokeDynamicInsnNode createInsn(@NotNull String name);
  @NotNull InvokeDynamicInsnNode createInsn(@NotNull MethodHandle methodHandle);
}
