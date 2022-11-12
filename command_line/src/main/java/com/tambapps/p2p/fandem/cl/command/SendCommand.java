package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.cl.SeekableSender;
import com.tambapps.p2p.fandem.cl.command.converter.AddressConverter;
import com.tambapps.p2p.fandem.cl.command.converter.NormalFileConverter;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The command to receive files
 */
@Parameters(separators = "=", commandDescription = "Send file to another peer")
public class SendCommand extends FandemCommand {

  private static final String TEXT_PROPERTIES_FILE = "send_text.properties";

  @Parameter(description = "path of the file to send", required = true,
      converter = NormalFileConverter.class)
  private List<File> files;

  @Parameter(names = {"-i", "--ip"}, description = "the ip address used to send (optional)",
      converter = AddressConverter.class)
  private InetAddress ip = null;

  @Parameter(names = {"-p", "--port"}, description = "the port used to send (optional)")
  private Integer port;

  @Parameter(names = {"-t", "--timeout"}, description = "the port used to send (optional)")
  private int timeout = 90 * 1000;

  public SendCommand() {
    super(TEXT_PROPERTIES_FILE);
  }

  @Override
  public void execute() {
    if (files.size() > 10) {
      System.out.println("You cannot send more than 10 files at once");
      System.exit(1);
      return;
    }

    InetAddress address;
    int port;
    try {
      address = this.ip != null ? this.ip : PeerUtils.getPrivateNetworkIpAddress();
      port = this.port != null ? this.port : PeerUtils.getAvailablePort(address, SenderPeer.DEFAULT_PORT);
    } catch (IOException e) {
      System.out.println("Error while looking for address/port to use: " + e.getMessage());
      System.exit(1);
      return;
    }

    try (SeekableSender sender = SeekableSender.create(address, port, timeout, this)) {
      try {
        System.out.println("Will send files " + files.stream().map(File::getName).collect(
            Collectors.joining(", ")));
        System.out.format("Waiting for a connection on %s (hex string %s)", sender.getPeer(), Fandem.toHexString(sender.getPeer()))
            .println();
        sender.send(files);
        System.out.println();
        System.out.println("File(s) were successfully sent");
      } catch (SocketException e) {
        System.out.println();
        System.out.println("Transfer was cancelled.");
      }
    } catch (SocketTimeoutException e) {
      System.out.println("No connection was made, canceling transfer");
    } catch (IOException e) {
      System.out.println("An error occurred while transferring file(s). Aborting transfer(s)");
    }
  }

}
