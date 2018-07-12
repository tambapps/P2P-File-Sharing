package com.tambapps.p2p.peer_transfer.desktop.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.tambapps.p2p.file_sharing.Peer;

@Parameters(separators = "=", commandDescription = "Receive file from another peer")
public class ReceiveCommand {
    @Parameter(names = "-peer", description = "the sending peer", required = true, converter = PeerConverter.class)
    private Peer peer;

    @Parameter(names = {"-download", "--downloadPath"}, description = "the path where the file(s) will be downloaded",
            required = true)
    private String downloadPath;

    @Parameter(names = "-count", description = "the number of files that will be received")
    private int count = 1;

    public Peer getPeer() {
        return peer;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public int getCount() {
        return count;
    }
}
