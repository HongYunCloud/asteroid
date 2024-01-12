package ink.bgp.asteroid.bukkit.transform;

import bot.inker.acj.JvmHacker;
import ink.bgp.asteroid.bukkit.AsteroidBukkitCore;
import ink.bgp.asteroid.core.spy.AsteroidSpyInternalService;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.transformer.IRawTransformer;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.invoke.MethodType;
import java.util.stream.Collectors;

public class CraftServerLoadPluginsTransform implements IRawTransformer {
  private final @NotNull AsteroidBukkitCore bukkitCore;
  private final @NotNull AsteroidSpyInternalService spyService;

  @Inject
  private CraftServerLoadPluginsTransform(
      final @NotNull AsteroidBukkitCore bukkitCore,
      final @NotNull AsteroidSpyInternalService spyService) {
    this.bukkitCore = bukkitCore;
    this.spyService = spyService;
  }

  @Override
  public @NotNull ClassNode transform(
      final @NotNull TransformerManager transformerManager,
      final @NotNull ClassNode transformedClass) {
    transformedClass.methods = transformedClass.methods
        .stream()
        .map(methodNode -> transformMethod(transformerManager, methodNode))
        .collect(Collectors.toList());
    return transformedClass;
  }

  @SneakyThrows
  public @NotNull MethodNode transformMethod(
      final @NotNull TransformerManager transformerManager,
      final @NotNull MethodNode methodNode) {
    if (methodNode.name.equals("loadPlugins") && methodNode.desc.equals("()V")) {
      final InsnList insn = new InsnList();

      insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
      insn.add(spyService.createInsn(
          JvmHacker.lookup()
              .findVirtual(AsteroidBukkitCore.class, "onLoadPlugins",
                  MethodType.methodType(void.class, Object.class))
              .bindTo(bukkitCore)
      ));

      methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insn);
    }
    return methodNode;
  }
}
