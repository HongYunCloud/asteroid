package ink.bgp.asteroid.core.log;

import net.lenni0451.classtransform.utils.log.ILogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class ClasstransformLogBridge implements ILogger {
  private final @NotNull Logger logger;

  public ClasstransformLogBridge(final @NotNull Logger logger) {
    this.logger = logger;
  }

  @Override
  public void info(final @NotNull String message, final @Nullable Object @Nullable ... args) {
    logger.info(message, args);
  }

  @Override
  public void warn(final @NotNull String message, final @Nullable Object @Nullable ... args) {
    logger.warn(message, args);
  }

  @Override
  public void error(final @NotNull String message, final @Nullable Object @Nullable ... args) {
    logger.error(message, args);
  }
}
