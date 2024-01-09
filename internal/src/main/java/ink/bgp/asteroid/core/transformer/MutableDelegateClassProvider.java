package ink.bgp.asteroid.core.transformer;

import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class MutableDelegateClassProvider implements IClassProvider {
  private final @NotNull List<@NotNull IClassProvider> delegates = new ArrayList<>();

  public MutableDelegateClassProvider(final @NotNull Iterable<@NotNull IClassProvider> delegates) {
    if (delegates instanceof Collection) {
      this.delegates.addAll((Collection<IClassProvider>) delegates);
    } else {
      delegates.forEach(this.delegates::add);
    }
  }

  public MutableDelegateClassProvider(final @NotNull IClassProvider @NotNull ... delegates) {
    this.delegates.addAll(Arrays.asList(delegates));
  }

  @Override
  public byte @NotNull [] getClass(final @NotNull String name) throws ClassNotFoundException {
    for (final IClassProvider delegate : delegates) {
      try {
        return delegate.getClass(name);
      } catch (ClassNotFoundException ignored) {
        //
      }
    }
    throw new ClassNotFoundException(name);
  }

  @Override
  public @NotNull Map<@NotNull String, @NotNull Supplier<byte @NotNull []>> getAllClasses() {
    final Map<String, Supplier<byte[]>> classes = new HashMap<>();

    for (final IClassProvider delegate : delegates) {
      classes.putAll(delegate.getAllClasses());
    }

    return classes;
  }
}
