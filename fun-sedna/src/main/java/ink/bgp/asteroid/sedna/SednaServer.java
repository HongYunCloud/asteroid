package ink.bgp.asteroid.sedna;

import ink.bgp.asteroid.sedna.util.io.LineBufferingOutputStream;
import ink.bgp.asteroid.sedna.util.io.TextStream;
import li.cil.sedna.api.Sizes;
import li.cil.sedna.api.device.BlockDevice;
import li.cil.sedna.api.device.PhysicalMemory;
import li.cil.sedna.buildroot.Buildroot;
import li.cil.sedna.device.block.ByteBufferBlockDevice;
import li.cil.sedna.device.memory.Memory;
import li.cil.sedna.device.rtc.GoldfishRTC;
import li.cil.sedna.device.rtc.SystemTimeRealTimeCounter;
import li.cil.sedna.device.serial.UART16550A;
import li.cil.sedna.device.virtio.VirtIOBlockDevice;
import li.cil.sedna.device.virtio.VirtIOFileSystemDevice;
import li.cil.sedna.fs.HostFileSystem;
import li.cil.sedna.riscv.R5Board;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SednaServer {
  private final int memorySize;
  private final @Nullable URL firmwareUrl;
  private final @Nullable URL imageUrl;
  private final @Nullable URL rootFsUrl;

  private final @NotNull R5Board board;
  private final @NotNull PhysicalMemory memory;
  private final @NotNull UART16550A consoleUart;
  private final @NotNull UART16550A rpcUart;
  private final @NotNull GoldfishRTC rtc;

  private final @NotNull BlockDevice rootfs;
  private final @NotNull VirtIOBlockDevice memoryHdd;
  private final @NotNull VirtIOFileSystemDevice hostFs;

  private final @NotNull LineBufferingOutputStream consoleOutputStream;

  private final int cyclesPerTick;
  private final int cyclesPerStep;
  private int cyclesRemaining;
  private long nextCycle;

  @SneakyThrows
  public SednaServer(
      final int memorySize,
      final @Nullable TextStream consoleStream,
      final @Nullable URL firmwareUrl,
      final @Nullable URL imageUrl,
      final @Nullable URL rootFsUrl) {
    this.memorySize = memorySize;
    this.firmwareUrl = firmwareUrl;
    this.imageUrl = imageUrl;
    this.rootFsUrl = rootFsUrl;

    this.board = new R5Board();
    this.memory = Memory.create(memorySize);
    this.consoleUart = new UART16550A();
    this.rpcUart = new UART16550A();
    this.rtc = new GoldfishRTC(SystemTimeRealTimeCounter.get());

    this.rootfs = ByteBufferBlockDevice.createFromStream(
        rootFsUrl == null ? Buildroot.getRootFilesystem() : rootFsUrl.openStream(),
        false);
    this.memoryHdd = new VirtIOBlockDevice(board.getMemoryMap(), rootfs);
    this.hostFs = new VirtIOFileSystemDevice(board.getMemoryMap(), "host_fs", new HostFileSystem());

    this.consoleOutputStream = new LineBufferingOutputStream(
        consoleStream == null ? System.out::print : consoleStream,
        "\n", 8192);

    this.cyclesPerTick = board.getCpu().getFrequency() / 20;
    this.cyclesPerStep = 1_000;
  }

  public void setupDevice() {
    consoleUart.getInterrupt().set(0xA, board.getInterruptController());
    rpcUart.getInterrupt().set(0xB, board.getInterruptController());
    rtc.getInterrupt().set(0xC, board.getInterruptController());
    memoryHdd.getInterrupt().set(0x1, board.getInterruptController());
    hostFs.getInterrupt().set(0x2, board.getInterruptController());

    board.addDevice(0x80000000L, memory);
    board.addDevice(consoleUart);
    board.addDevice(rpcUart);
    board.addDevice(rtc);
    board.addDevice(memoryHdd);
    board.addDevice(hostFs);

    board.setBootArguments("root=/dev/vda rw");
    board.setStandardOutputDevice(consoleUart);

    board.reset();
  }

  @SneakyThrows
  public void loadProgram() {
    loadProgramFile(firmwareUrl == null ? Buildroot.getFirmware() : firmwareUrl.openStream(), 0x000000);
    loadProgramFile(imageUrl == null ? Buildroot.getLinuxImage() : imageUrl.openStream(), 0x200000);

    board.initialize();
  }

  public void setRunning() {
    board.setRunning(true);
  }

  public boolean isRunning() {
    return board.isRunning();
  }

  public void enableGdb(int port, boolean block) {
    board.enableGDB(port, block);
  }

  @SneakyThrows
  public void runCycle() {
    final long stepStart = System.currentTimeMillis();
    if(nextCycle > stepStart) {
      return;
    }

    cyclesRemaining += cyclesPerTick;
    while (cyclesRemaining > 0) {
      board.step(cyclesPerStep);
      cyclesRemaining -= cyclesPerStep;

      int value;
      while ((value = consoleUart.read()) != -1) {
        consoleOutputStream.write(value);
      }
    }

    consoleOutputStream.flush();

    if (board.isRestarting()) {
      loadProgram();
    }

    nextCycle = stepStart + 50;
  }

  public void writeConsole(final @NotNull String line) {
    for (byte b : line.getBytes(StandardCharsets.UTF_8)) {
      consoleUart.putByte(b);
    }
  }

  public void writeRpc(final byte @NotNull [] message) {
    for (byte b : message) {
      rpcUart.putByte(b);
    }
  }

  @SneakyThrows
  public void loadProgramFile(final InputStream stream, final int offset) {
    final BufferedInputStream bis = new BufferedInputStream(stream);
    for (int address = offset, value = bis.read(); value != -1; value = bis.read(), address++) {
      memory.store(address, (byte) value, Sizes.SIZE_8_LOG2);
    }
  }
}
