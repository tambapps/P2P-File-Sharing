package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.fandem.SenderPeer;
import com.tambapps.p2p.fandem.cl.FandemMode;
import com.tambapps.p2p.fandem.cl.Receiver;
import com.tambapps.p2p.fandem.cl.command.converter.ExistingFileConverter;
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

@Setter
@Parameters(separators = "=", commandDescription = "Receive file from another peer. If no peer is provided, the program will look for it")
public class ReceiveCommand extends FandemCommand {
    @Parameter(names = {"-p", "-peer"}, description = "the sending peer (in peer notation or hexString)", converter = PeerConverter.class)
    private Peer peer;

    @Parameter(names = {"-d", "--download"}, description = "the file/directory where the file will be downloaded",
    converter = ExistingFileConverter.class)
    private File downloadDirectory = new File(System.getProperty("user.dir"));

    public ReceiveCommand() {
        super(FandemMode.RECEIVE);
    }

    @Override
    public void execute() {
        Peer peer = this.peer != null ? this.peer : seekSendingPeer();
        if (peer != null) {
            Receiver receiver = new Receiver(peer, downloadDirectory, this);
            System.out.println("Connecting to " + peer);
            try {
                List<File> files = receiver.receive();
                System.out.println();
                System.out.println("Received successfully files " +
                    files.stream()
                        .map(File::getName)
                        .collect(Collectors.joining(", ")));
                System.out.println("Directory: " + downloadDirectory.getAbsolutePath());
            } catch (HandshakeFailException e) {
                System.out.println("Error while communicating with other peer: " + e.getMessage());
            } catch (IOException e) {
                System.out.println();
                System.out.println("Error while receiving files: " + e.getMessage());
            }
        }
    }

    private Peer seekSendingPeer() {
        System.out.println("Looking for a sending peer...");
        try (Scanner scanner = new Scanner(System.in)) {
            return seekSendingPeer(scanner);
        }
    }

    private SenderPeer seekSendingPeer(Scanner scanner) {
        DatagramSupplier<List<SenderPeer>> sniffSupplier = null;
        try {
            sniffSupplier = Fandem.senderPeersSupplier();
        } catch (IOException e) {
            System.out.println("Couldn't start seeking peers: " + e.getMessage());
            System.exit(1);
        }
        while (true) {
            try {
                List<SenderPeer> senderPeers = sniffSupplier.get();
                for (SenderPeer senderPeer : senderPeers) {
                    System.out.format(
                            "%s wants to send\n%s.\nReceive the file(s)? (Tap 'y' for yes ,'n' for no or 's' to stop)",
                            senderPeer.getDeviceName(),
                            senderPeer.getFiles()
                                .stream()
                                .map(f -> String.format("%s (%s)", f.getFileName(), FileUtils.toFileSize(f.getFileSize())))
                                .collect(Collectors.joining("\n- ", "- ", "")))
                        .println();
                    switch (scanner.nextLine().toLowerCase().charAt(0)) {
                        case 'y':
                            return senderPeer;
                        case 's':
                            return null;
                        // default do nothing
                    }
                }
            } catch (IOException e) {
                return null;
            }
        }
    }

}
