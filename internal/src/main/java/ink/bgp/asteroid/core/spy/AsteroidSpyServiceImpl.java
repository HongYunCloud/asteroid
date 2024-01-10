package ink.bgp.asteroid.core.spy;

import bot.inker.acj.JvmHacker;
import ink.bgp.asteroid.api.spy.AsteroidSpyService;
import ink.bgp.asteroid.core.util.DefineClassUtil;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.invoke.*;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class AsteroidSpyServiceImpl implements AsteroidSpyService {
  private static final @NotNull String SPY_CLASS_NAME = "ink.bgp.asteroid.core.spy.$AsteroidSpy";
  private static final @NotNull String SPY_CLASS_INTERNAL_NAME = "ink/bgp/asteroid/core/spy/$AsteroidSpy";

  private final @NotNull Map<@NotNull String, @NotNull CallSite> callSiteMap = new ConcurrentHashMap<>();
  private @NotNull BigInteger idAlloc = BigInteger.ZERO;

  @Inject
  private AsteroidSpyServiceImpl() {

  }

  @SneakyThrows
  public void load() {
    injectDepends(SPY_CLASS_INTERNAL_NAME);
    final Class<?> spyClass = Class.forName(SPY_CLASS_NAME, false, null);
    final MethodHandle loadImplHandle = JvmHacker.lookup()
        .findVirtual(
            AsteroidSpyServiceImpl.class,
            "getHandle",
            MethodType.methodType(CallSite.class, String.class))
        .bindTo(this);
    JvmHacker.lookup()
        .findStatic(spyClass, "$setHandle", MethodType.methodType(void.class, MethodHandle.class))
        .invokeExact(loadImplHandle);
  }

  private synchronized @NotNull String nextId() {
    byte[] bytes = idAlloc.toByteArray();
    idAlloc = idAlloc.add(BigInteger.ONE);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  @Override
  public @NotNull CallSite getHandle(final @NotNull String name) {
    CallSite result = callSiteMap.get(name);
    if (result == null) {
      throw new IllegalStateException("spy callsite '" + name + "' not found");
    }
    return result;
  }

  @Override
  public @NotNull String saveConstantHandle(final @NotNull MethodHandle methodHandle) {
    final String id = nextId();
    callSiteMap.put(id, new ConstantCallSite(methodHandle));
    return id;
  }

  @Override
  public @NotNull String saveMutableHandle(final @NotNull MethodHandle methodHandle) {
    final String id = nextId();
    callSiteMap.put(id, new MutableCallSite(methodHandle));
    return id;
  }

  @Override
  public void setMutableHandle(final @NotNull String name, final @NotNull MethodHandle methodHandle) {
    final CallSite callSite = getHandle(name);
    if (!(callSite instanceof MutableCallSite)) {
      throw new IllegalStateException("key " + name + " is not mutable");
    }
    callSite.setTarget(methodHandle);
  }

  public @NotNull InvokeDynamicInsnNode createInsn(final @NotNull String name) {
    final CallSite callSite = getHandle(name);
    final String descriptor = callSite.type().toMethodDescriptorString();
    return new InvokeDynamicInsnNode(
        name,
        descriptor,
        new Handle(
            Opcodes.H_INVOKESTATIC,
            SPY_CLASS_INTERNAL_NAME,
            "$bootstrap",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
            false)
    );
  }

  public @NotNull InvokeDynamicInsnNode createInsn(final @NotNull MethodHandle methodHandle) {
    return createInsn(saveConstantHandle(methodHandle));
  }

  @SneakyThrows
  private void injectDepends(final @NotNull String name) {
    try {
      Class.forName(name, false, null);
    } catch (ClassNotFoundException e) {
      try (final InputStream in = AsteroidSpyServiceImpl.class.getClassLoader().getResourceAsStream(name + ".class")) {
        if (in == null) {
          throw new IllegalStateException("runtime class " + name + " not found");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = in.read(buf)) > 0) baos.write(buf, 0, len);
        byte[] bytes =  baos.toByteArray();
        DefineClassUtil.defineClass(name, bytes, 0, bytes.length, null, null);
      } catch (Throwable ex) {
        ex.printStackTrace();
      }
    }
  }
}
