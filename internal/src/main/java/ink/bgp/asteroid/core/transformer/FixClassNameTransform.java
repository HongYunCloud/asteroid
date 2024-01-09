package ink.bgp.asteroid.core.transformer;

import net.lenni0451.classtransform.utils.log.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.ProtectionDomain;
import java.util.Base64;

public final class FixClassNameTransform implements ClassFileTransformer {
  private final @NotNull ClassFileTransformer classFileTransformer;

  public FixClassNameTransform(@NotNull ClassFileTransformer classFileTransformer) {
    this.classFileTransformer = classFileTransformer;
  }

  @Override
  public byte[] transform(
      final ClassLoader loader,
      String className,
      final Class<?> classBeingRedefined,
      final ProtectionDomain protectionDomain,
      final byte[] classfileBuffer
  ) throws IllegalClassFormatException {
    if (className == null) {
      try {
        className = getClassName(ByteBuffer.wrap(classfileBuffer));
      } catch (Throwable e) {
        Logger.error("Failed to get class name from bytes \n{}",
            Base64.getEncoder().encode(classfileBuffer), e);
      }
    }
    return classFileTransformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
  }

  private static @NotNull String getClassName(final @NotNull ByteBuffer buf) {
    if (buf.order(ByteOrder.BIG_ENDIAN).getInt() != 0xCAFEBABE) {
      throw new IllegalArgumentException("not a valid class file");
    }
    buf.getChar();
    buf.getChar();
    final int poolSize = buf.getChar();
    int[] pool = new int[poolSize];

    for (int ix = 1; ix < poolSize; ix++) {
      byte tag = buf.get();
      switch (tag) {
        case 1: // Utf8
          cast(buf).position((pool[ix] = cast(buf).position()) + buf.getChar() + 2);
          continue;
        case 7: // Class
        case 8: // String
        case 16: // MethodType
        case 19: // Module
        case 20: // Package
          pool[ix] = buf.getChar();
          break;
        case 3: // Integer
        case 4: // Float
        case 9: // FieldRef
        case 10: // MethodRef
        case 11: // InterfaceMethodRef
        case 12: // NameAndType
        case 17: // Dynamic
        case 18: // InvokeDynamic
          cast(buf).position(cast(buf).position() + 4);
          break;
        case 5: // Long
        case 6: // Double
          cast(buf).position(cast(buf).position() + 8);
          ix++;
          break;
        case 15: // MethodHandle
          cast(buf).position(cast(buf).position() + 3);
          break;
        default:
          throw new IllegalStateException("unknown pool item type " + buf.get(cast(buf).position() - 1));
      }
    }
    buf.getChar();
    final int thisClass = buf.getChar();
    cast(buf).position(pool[pool[thisClass]]);
    return decodeString(buf);
  }

  private static @NotNull String decodeString(final @NotNull ByteBuffer buf) {
    final int size = buf.getChar();
    final int oldLimit = cast(buf).limit();
    cast(buf).limit(cast(buf).position() + size);
    final StringBuilder builder = new StringBuilder(size + (size >> 1));
    while (buf.hasRemaining()) {
      final byte b = buf.get();
      if (b > 0) {
        builder.append((char) b);
      }
      else {
        int b2 = buf.get();
        if ((b & 0xf0) != 0xe0) {
          builder.append((char) ((b & 0x1F) << 6 | b2 & 0x3F));
        } else {
          int b3 = buf.get();
          builder.append((char) ((b & 0x0F) << 12 | (b2 & 0x3F) << 6 | b3 & 0x3F));
        }
      }
    }
    cast(buf).limit(oldLimit);
    return builder.toString();
  }

  @SuppressWarnings("RedundantCast")
  public static <T extends Buffer> Buffer cast(T byteBuffer) {
    return (Buffer) byteBuffer;
  }
}