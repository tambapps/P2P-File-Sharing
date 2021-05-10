package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;
import com.tambapps.p2p.fandem.cl.command.converter.ExistingFileConverter;
import com.tambapps.p2p.speer.Peer;

import java.io.File;
import java.util.Optional;

@Parameters(separators = "=", commandDescription = "Receive file from another peer. If no peer is provided, the program will look for it")
public class ReceiveCommand {
    @Parameter(names = {"-p", "-peer"}, description = "the sending peer (in peer notation or hexString)", converter = PeerConverter.class)
    private Peer peer;

    @Parameter(names = {"-d", "--downloadPath"}, description = "the path where the file will be "
        + ". If it is a directory, a new file will be created in it. If it is a file, the received "
        + "file will be downloaded in it",
    converter = FileConverter.class)
    private File downloadFile = new File(System.getProperty("user.dir"));

    @Parameter(names = {"-c", "-count"}, description = "the number of files that will be received")
    private int count = 1;

    public Optional<Peer> getPeer() {
        return Optional.ofNullable(peer);
    }

    public File getDownloadFile() {
        return downloadFile;
    }

    public int getCount() {
        return count;
    }

}
