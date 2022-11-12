package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.FileReceiver;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.cl.command.converter.ExistingFileConverter;
import com.tambapps.p2p.fandem.cl.command.converter.PeerConverter;
import com.tambapps.p2p.fandem.util.FileProvider;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.speer.Peer;
import com.tambapps.p2p.speer.datagram.DatagramSupplier;
import com.tambapps.p2p.speer.exception.HandshakeFailException;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * The command to send files
 */
@Setter
@Parameters(separators = "=", commandDescription = "Receive file from another peer. If no peer is provided, the program will look for it")
public class ReceiveCommand extends FandemCommand {

  private static final String TEXT_PROPERTIES_FILE = "receive_text.properties";
  @Parameter(names = {"-p", "-peer"}, description = "the sending peer (in peer notation or hexString)", converter = PeerConverter.class)
  private Peer peer;

  @Parameter(names = {"-d", "--download-path"}, description = "the file/directory where the file will be downloaded",
      converter = ExistingFileConverter.class)
  private File downloadFile = new File(System.getProperty("user.dir"));

  public ReceiveCommand() {
    super(TEXT_PROPERTIES_FILE);
  }

  @Override
  public void execute() {
    Peer senderPeer = this.peer != null ? this.peer : seekSendingPeer();
    if (senderPeer == null) {
      // seek may have been cancelled, or an IO exception could have occured
      return;
    }
    System.out.println("Connecting to " + senderPeer);
    try {
      FileReceiver fileReceiver = new FileReceiver(this);
      FileProvider fileProvider = downloadFile.isDirectory()
          ? FileUtils.availableFileInDirectoryProvider(downloadFile)
          : (ignored) -> FileUtils.newAvailableFile(downloadFile.getParentFile(), downloadFile.getName());

      List<File> files = fileReceiver.receiveFrom(senderPeer, fileProvider);
      System.out.println();
      System.out.format("Received successfully file(s) %s in %s", files.stream()
          .map(File::getName)
          .collect(Collectors.joining(", ")), getDownloadDirectory().getAbsolutePath())
          .println();
    } catch (HandshakeFailException e) {
      System.out.println("Error while communicating with other peer: " + e.getMessage());
    } catch (IOException e) {
      System.out.println();
      System.out.println("Error while receiving files: " + e.getMessage());
    }
  }

  private Peer seekSendingPeer() {
    System.out.println("Looking for a sending peer...");
    try (Scanner scanner = new Scanner(System.in)) {
      return seekSendingPeer(scanner);
    }
  }

  private SenderPeer seekSendingPeer(Scanner scanner) {
    try (DatagramSupplier<List<SenderPeer>> senderPeersSupplier = Fandem.senderPeersSupplier()) {
      return proposePeer(scanner, senderPeersSupplier);
    } catch (IOException e) {
      System.out.println("Couldn't start seeking peers: " + e.getMessage());
      System.exit(1);
      return null;
    }
  }

  private SenderPeer proposePeer(Scanner scanner, DatagramSupplier<List<SenderPeer>> senderPeersSupplier) throws IOException {
    while (true) {
      List<SenderPeer> senderPeers = senderPeersSupplier.get();
      for (SenderPeer senderPeer : senderPeers) {
        System.out.format(
                "%s wants to send\n%s.\nReceive the file(s)? (Tap 'y' for yes ,'n' for no or 's' to stop)",
                senderPeer.getDeviceName(),
                senderPeer.getFiles()
                    .stream()
                    .map(f -> "%s (%s)".formatted(f.getFileName(), FileUtils.toFileSize(f.getFileSize())))
                    .collect(Collectors.joining("\n- ", "- ", "")))
            .println();
        switch (scanner.nextLine().toLowerCase().charAt(0)) {
          case 'y':
            return senderPeer;
          case 's':
            System.out.println("Stopped looking for sending peers");
            return null;
          // default do nothing
        }
      }
    }
  }

  private File getDownloadDirectory() {
      return downloadFile.isDirectory() ? downloadFile : downloadFile.getParentFile();
  }
}
