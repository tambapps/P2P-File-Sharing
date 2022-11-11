package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.cl.Mode;
import com.tambapps.p2p.fandem.cl.Sender;
import com.tambapps.p2p.fandem.cl.command.converter.AddressConverter;
import com.tambapps.p2p.fandem.cl.command.converter.RealFileConverter;
import com.tambapps.p2p.fandem.cl.exception.SendingException;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Parameters(separators = "=", commandDescription = "Send file to another peer")
public class SendCommand extends FandemCommand {

    @Parameter(description = "path of the file to send", required = true,
        converter = RealFileConverter.class)
    private List<File> files;

    @Parameter(names = "-ip", description = "the ip used to send (optional)",
        converter = AddressConverter.class)
    private InetAddress ip = null;

    @Parameter(names = {"-p", "--port"}, description = "the port used to send (optional)")
    private Integer port;

    @Parameter(names = {"-t", "--timeout"}, description = "the port used to send (optional)")
    private int timeout = 90 * 1000;

    public SendCommand() {
        super(Mode.SEND);
    }

    @Override
    public void execute() {
        if (files.size() > 10) {
            System.out.println("You cannot send more than 10 files at once");
            System.exit(1);
            return;
        }
        try (Sender sender = Sender.create(this, this)) {
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
        } catch (SendingException e) {
            System.out.println(e.getMessage());
        } catch (SocketTimeoutException e) {
            System.out.println("No connection was made, canceling transfer");
        } catch (IOException e) {
            System.out.println("An error occurred while transferring file(s). Aborting transfer(s)");
        }
    }

    public List<File> getFiles() {
        return files;
    }

    public Optional<InetAddress> getIp() {
        return Optional.ofNullable(ip != null ? ip : PeerUtils.getPrivateNetworkIpAddressOrNull());
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    public int getTimeout() {
        return timeout;
    }
}
