package ink.bgp.asteroid.loader;

import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    printExtra(writer, null);
    for (val line : LOGO) {
      writer.append(line + "\n");
    }
    printExtra(writer, extraMessage);
  }

  @SneakyThrows
  private static void printExtra(final @NotNull Appendable writer, final @Nullable String extraMessage) {
    if (extraMessage == null) {
      writer.append(EMPTY_LINE + "\n");
    } else {
      final int beforeLine = (EMPTY_LINE.length() - extraMessage.length()) / 2;
      writer.append(EMPTY_LINE.substring(0, beforeLine) + extraMessage +
          EMPTY_LINE.substring(beforeLine + extraMessage.length()) + "\n");
    }
  }
}
