package ink.bgp.asteroid.forcespy.transform;

import ink.bgp.asteroid.core.spy.AsteroidSpyServiceImpl;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.lenni0451.classtransform.transformer.IBytecodeTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

@Singleton
public final class ForceSpyTransform implements IBytecodeTransformer {
  private final @NotNull AsteroidSpyServiceImpl spyService;

  @Inject
  private ForceSpyTransform(final @NotNull AsteroidSpyServiceImpl spyService) {
    this.spyService = spyService;
  }

  @Override
  public byte[] transform(
      final @NotNull String className,
      final byte @NotNull [] bytecode,
      final boolean calculateStackMapFrames) {
    final boolean[] visitedFlag = new boolean[]{ false };
    ClassWriter writer = new ClassWriter(0);
    new ClassReader(bytecode).accept(
        new RedirectVisitor(writer, visitedFlag), 0);
    return visitedFlag[0] ? writer.toByteArray() : null;
  }

  private final class RedirectVisitor extends ClassVisitor {
    private final boolean @NotNull [] visitedFlag;

    public RedirectVisitor(final @NotNull ClassVisitor classVisitor, final boolean @NotNull [] visitedFlag) {
      super(Opcodes.ASM9, classVisitor);
      this.visitedFlag = visitedFlag;
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final @NotNull String name,
        final @NotNull String descriptor,
        final @Nullable String signature,
        final @NotNull String @Nullable [] exceptions) {
      if(((access & Opcodes.ACC_STATIC) == 0) && name.equals("loadClass") && descriptor.equals("(Ljava/lang/String;Z)Ljava/lang/Class;")) {
        visitedFlag[0] = true;
        return new RedirectMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
      } else {
        return super.visitMethod(access, name, descriptor, signature, exceptions);
      }
    }
  }

  private final class RedirectMethodVisitor extends MethodVisitor {
    public RedirectMethodVisitor(final @NotNull MethodVisitor methodVisitor) {
      super(Opcodes.ASM9, methodVisitor);
    }

    @Override
    public void visitCode() {
      super.visitCode();

      final Label continueLabel = new Label();

      visitVarInsn(Opcodes.ALOAD, 1);
      visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/ClassLoader");
      visitJumpInsn(Opcodes.IFEQ, continueLabel);

      visitLdcInsn(AsteroidSpyServiceImpl.SPY_CLASS_NAME);
      visitVarInsn(Opcodes.ALOAD, 1);
      visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
      visitJumpInsn(Opcodes.IFEQ, continueLabel);

      visitLdcInsn(AsteroidSpyServiceImpl.SPY_CLASS_NAME);
      visitInsn(Opcodes.ICONST_1);
      visitInsn(Opcodes.ACONST_NULL);
      visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;", false);
      visitInsn(Opcodes.ARETURN);

      visitLabel(continueLabel);
    }
  }
}
