package ink.bgp.asteroid.sedna.util.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Log4jTextStream implements TextStream {
  private final @NotNull Logger logger;

  public Log4jTextStream(final @NotNull String name) {
    this.logger = LogManager.getLogger(name);
  }

  @Override
  public void text(final @NotNull String text) {
    if (logger.isInfoEnabled()) {
      logger.info(text.endsWith("\n") ? text.substring(0, text.length() - 1) : text);
    }
  }

  @Override
  public void endOfStream(final @Nullable Throwable failure) {
    if (failure != null) {
      logger.error("text stream ended", failure);
    }
  }
}
