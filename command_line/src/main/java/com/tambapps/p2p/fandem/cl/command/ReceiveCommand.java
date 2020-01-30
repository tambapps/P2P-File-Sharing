package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.tambapps.p2p.fandem.Peer;
import com.tambapps.p2p.fandem.cl.command.converter.AddressConverter;
import com.tambapps.p2p.fandem.cl.command.converter.DirectoryFileConverter;

import java.io.File;
import java.net.InetAddress;
import java.util.Optional;

@Parameters(separators = "=", commandDescription = "Receive file from another peer. If no peer is provided, the program will look for it")
public class ReceiveCommand {
    @Parameter(names = {"-p", "-peer"}, description = "the sending peer (in peer notation or hexString)", converter = PeerConverter.class)
    private Peer peer;

    @Parameter(names = {"-d", "--downloadPath"}, description = "the path where the file(s) will be downloaded",
    converter = DirectoryFileConverter.class)
    private File downloadDirectory = new File(System.getProperty("user.dir"));

    @Parameter(names = {"-c", "-count"}, description = "the number of files that will be received")
    private int count = 1;

    public Optional<Peer> getPeer() {
        return Optional.ofNullable(peer);
    }

    public File getDownloadDirectory() {
        return downloadDirectory;
    }

    public int getCount() {
        return count;
    }

}
