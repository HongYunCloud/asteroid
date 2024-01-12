package ink.bgp.asteroid.core.log;

import org.apache.ivy.util.AbstractMessageLogger;
import org.apache.ivy.util.Message;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class IvyLogBridge extends AbstractMessageLogger {
  private final @NotNull Logger logger;

  public IvyLogBridge(final @NotNull Logger logger) {
    this.logger = logger;
  }

  @Override
  protected void doProgress() {
    //
  }

  @Override
  protected void doEndProgress(String msg) {
    logger.info(msg);
  }

  @Override
  public void log(String msg, int level) {
    switch (level) {
      case Message.MSG_ERR: {
        logger.error(msg);
        break;
      }
      case Message.MSG_WARN: {
        logger.warn(msg);
        break;
      }
      case Message.MSG_INFO: {
        logger.info(msg);
        break;
      }
      case Message.MSG_VERBOSE: {
        logger.debug(msg);
        break;
      }
      case Message.MSG_DEBUG: {
        logger.trace(msg);
        break;
      }
      default: {
        logger.warn("Unknown Ivy message level: {}", level);
        logger.warn(msg);
      }
    }
  }

  @Override
  public void rawlog(String msg, int level) {
    log(msg, level);
  }
}
