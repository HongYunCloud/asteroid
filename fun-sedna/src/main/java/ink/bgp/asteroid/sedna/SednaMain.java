package ink.bgp.asteroid.sedna;

import ink.bgp.asteroid.sedna.util.io.Log4jTextStream;
import li.cil.sedna.Sedna;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class SednaMain {
  private static final @NotNull Logger logger = LogManager.getLogger();

  public static void main(final String[] args) throws Exception {
    Thread.currentThread().setContextClassLoader(SednaMain.class.getClassLoader());

    Sedna.initialize();

    SednaServer server = new SednaServer(
        32 * 1024 * 1024,
        new Log4jTextStream("ink.bgp.asteroid.vm.main"),
        null,
        null,
        null);

    server.setupDevice();
    server.loadProgram();
    server.setRunning();

    while (server.isRunning()) {
      server.runCycle();
    }
  }
}