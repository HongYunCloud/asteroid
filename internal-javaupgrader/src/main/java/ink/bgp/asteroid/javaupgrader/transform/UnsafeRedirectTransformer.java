package ink.bgp.asteroid.javaupgrader.transform;

import bot.inker.acj.JvmHacker;
import ink.bgp.asteroid.core.spy.AsteroidSpyServiceImpl;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import net.lenni0451.classtransform.transformer.IBytecodeTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.security.ProtectionDomain;
import java.util.Objects;

import static java.lang.invoke.MethodHandles.*;

@Singleton
public final class UnsafeRedirectTransformer implements IBytecodeTransformer {
  private final @NotNull AsteroidSpyServiceImpl spyService;
  private @Nullable InvokeDynamicInsnNode defineClassNode;

  @Inject
  private UnsafeRedirectTransformer(final @NotNull AsteroidSpyServiceImpl spyService) {
    this.spyService = spyService;
  }

  @SneakyThrows
  public void load() {
    MethodHandle defineClassHandle;
    try {
      final Class<?> jdkUnsafeClass = Class.forName("jdk.internal.misc.Unsafe", false, null);
      final Object jdkUnsafe = JvmHacker.lookup()
          .findStaticGetter(jdkUnsafeClass, "theUnsafe", jdkUnsafeClass)
          .invoke();
      defineClassHandle = JvmHacker.lookup()
          .findVirtual(jdkUnsafeClass, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class))
          .bindTo(jdkUnsafe);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      final Class<?> sunUnsafeClass = Class.forName("sun.misc.Unsafe", false, null);
      defineClassHandle = JvmHacker.lookup()
          .findVirtual(sunUnsafeClass, "defineClass", MethodType.methodType(Class.class, String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class))
          .bindTo(JvmHacker.unsafe());
    }

    defineClassNode = spyService.createInsn(filterArguments(
        dropArguments(defineClassHandle, 0, Unsafe.class),
        0,
        lookup().findStatic(
            Objects.class,
            "requireNonNull",
            MethodType.methodType(Object.class, Object.class)
        ).asType(MethodType.methodType(Unsafe.class, Unsafe.class))
    ));
  }

  @Override
  public byte[] transform(
      final @NotNull String className,
      final byte @NotNull [] bytecode,
      final boolean calculateStackMapFrames) {
    ClassWriter writer = new ClassWriter(0);
    new ClassReader(bytecode).accept(
        new RedirectVisitor(writer), 0);
    return writer.toByteArray();
  }

  private final class RedirectVisitor extends ClassVisitor {
    public RedirectVisitor(final @NotNull ClassVisitor classVisitor) {
      super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final @NotNull String name,
        final @NotNull String descriptor,
        final @Nullable String signature,
        final @NotNull String @Nullable [] exceptions) {
      return new RedirectMethodVisitor(
          super.visitMethod(access, name, descriptor, signature, exceptions));
    }
  }

  private final class RedirectMethodVisitor extends MethodVisitor {
    public RedirectMethodVisitor(final @NotNull MethodVisitor methodVisitor) {
      super(Opcodes.ASM9, methodVisitor);
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final @NotNull String owner,
        final @NotNull String name,
        final @NotNull String descriptor,
        final boolean isInterface) {
      if ((opcode == Opcodes.INVOKESPECIAL || opcode == Opcodes.INVOKEVIRTUAL)
          && "sun/misc/Unsafe".equals(owner)
          && "defineClass".equals(name)
          && "(Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;".equals(descriptor)
      ) {
        defineClassNode.accept(this);
      } else {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
      }
    }
  }
}
