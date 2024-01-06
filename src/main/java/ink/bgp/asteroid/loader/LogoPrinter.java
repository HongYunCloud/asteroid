package ink.bgp.asteroid.loader;

import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.io.PrintWriter;

/* package-private */ final class LogoPrinter {
  private static final @NotNull String @NotNull [] LOGO = new String[]{
      "  █████  ███████ ████████ ███████ ██████   ██████  ██ ██████  ",
      " ██   ██ ██         ██    ██      ██   ██ ██    ██ ██ ██   ██ ",
      " ███████ ███████    ██    █████   ██████  ██    ██ ██ ██   ██ ",
      " ██   ██      ██    ██    ██      ██   ██ ██    ██ ██ ██   ██ ",
      " ██   ██ ███████    ██    ███████ ██   ██  ██████  ██ ██████  "
  };
  private static final @NotNull String EMPTY_LINE =
      "                                                              ";

  @SneakyThrows
  public static void print(final @NotNull Appendable writer, final @Nullable String extraMessage) {
    for (val line : LOGO) {
      writer.append(line + "\n");
    }
    if (extraMessage != null) {
      final int beforeLine = (EMPTY_LINE.length() - extraMessage.length()) / 2;
      writer.append(EMPTY_LINE.substring(0, beforeLine) + extraMessage +
          EMPTY_LINE.substring(beforeLine + extraMessage.length()) + "\n");
    }
  }
}
