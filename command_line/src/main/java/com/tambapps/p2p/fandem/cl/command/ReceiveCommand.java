package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.tambapps.p2p.speer.Peer;

import java.io.File;
import java.util.Optional;

@Parameters(separators = "=", commandDescription = "Receive file from another peer. If no peer is provided, the program will look for it")
public class ReceiveCommand {
    @Parameter(names = {"-p", "-peer"}, description = "the sending peer (in peer notation or hexString)", converter = PeerConverter.class)
    private Peer peer;

    @Parameter(names = {"-d", "--downloadDir"}, description = "the directory where the file will be downloaded",
    converter = FileConverter.class)
    private File downloadDirectory = new File(System.getProperty("user.dir"));

    public Optional<Peer> getPeer() {
        return Optional.ofNullable(peer);
    }

    public File getDownloadDirectory() {
        return downloadDirectory;
    }

}
