package ink.bgp.asteroid.bukkit.transform;

import bot.inker.acj.JvmHacker;
import ink.bgp.asteroid.bukkit.AsteroidBukkitCore;
import ink.bgp.asteroid.core.spy.AsteroidSpyInternalService;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.transformer.IRawTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.invoke.MethodType;
import java.net.URLClassLoader;
import java.util.stream.Collectors;

public class PluginClassLoaderTransform implements IRawTransformer {
  private final @NotNull AsteroidBukkitCore bukkitCore;
  private final @NotNull AsteroidSpyInternalService spyService;

  private @Nullable String onPluginLoaderCreatedKey;
  private @Nullable String onPluginLoaderLoadClassKey;

  @Inject
  private PluginClassLoaderTransform(
      final @NotNull AsteroidBukkitCore bukkitCore,
      final @NotNull AsteroidSpyInternalService spyService) {
    this.bukkitCore = bukkitCore;
    this.spyService = spyService;
  }

  @SneakyThrows
  public void load() {
    this.onPluginLoaderCreatedKey = spyService.saveConstantHandle(JvmHacker.lookup()
        .findVirtual(AsteroidBukkitCore.class, "onPluginLoaderCreated",
            MethodType.methodType(void.class, URLClassLoader.class, String.class))
        .bindTo(bukkitCore));
    this.onPluginLoaderLoadClassKey = spyService.saveConstantHandle(JvmHacker.lookup()
        .findVirtual(AsteroidBukkitCore.class, "onPluginLoaderLoadClass",
            MethodType.methodType(Class.class, URLClassLoader.class, String.class, String.class))
        .bindTo(bukkitCore));
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
    if (methodNode.name.equals("<init>") && methodNode.desc.endsWith(")V")) {
      transformInitMethod(methodNode);
    } else if (methodNode.name.startsWith("loadClass") && methodNode.desc.startsWith("(Ljava/lang/String;") && methodNode.desc.endsWith(")Ljava/lang/Class;")) {
      transformLoadMethod(methodNode);
    }
    return methodNode;
  }

  @SneakyThrows
  private void transformInitMethod(final @NotNull MethodNode methodNode) {
    final InsnList insn = new InsnList();

    insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
    insertGetPluginName(insn);
    insn.add(spyService.createInsn(onPluginLoaderCreatedKey));

    for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
      if(insnNode.getOpcode() == Opcodes.RETURN) {
        methodNode.instructions.insertBefore(insnNode, insn);
      }
    }
  }

  @SneakyThrows
  private void transformLoadMethod(final @NotNull MethodNode methodNode) {
    final InsnList insn = new InsnList();

    insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
    insertGetPluginName(insn);
    insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
    insn.add(spyService.createInsn(onPluginLoaderLoadClassKey));
    insn.add(new InsnNode(Opcodes.DUP));

    final LabelNode continueLabel = new LabelNode();
    insn.add(new JumpInsnNode(Opcodes.IFNULL, continueLabel));
    insn.add(new InsnNode(Opcodes.ARETURN));
    insn.add(continueLabel);
    insn.add(new InsnNode(Opcodes.POP));

    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insn);
  }

  private void insertGetPluginName(final @NotNull InsnList insn) {
    insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
    insn.add(new FieldInsnNode(
        Opcodes.GETFIELD,
        "org/bukkit/plugin/java/PluginClassLoader",
        "description",
        "Lorg/bukkit/plugin/PluginDescriptionFile;"));
    insn.add(new MethodInsnNode(
        Opcodes.INVOKEVIRTUAL,
        "org/bukkit/plugin/PluginDescriptionFile",
        "getName",
        "()Ljava/lang/String;",
        false));
  }
}
