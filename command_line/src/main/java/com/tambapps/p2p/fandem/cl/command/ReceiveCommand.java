package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.tambapps.p2p.fandem.Peer;

@Parameters(separators = "=", commandDescription = "Receive file from another peer. If no peer is provided, the program will look for it")
public class ReceiveCommand {
    @Parameter(names = {"-p", "-peer"}, description = "the sending peer (in peer notation or hexString)", converter = PeerConverter.class)
    private Peer peer;

    @Parameter(names = {"-d", "--downloadPath"}, description = "the path where the file(s) will be downloaded")
    private String downloadPath = System.getProperty("user.dir");

    @Parameter(names = {"-c", "-count"}, description = "the number of files that will be received")
    private int count = 1;

    @Parameter(names = "-ip", description = "an example ip used to sniff from (optional)")
    private String ip = null;

    public Peer getPeer() {
        return peer;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public int getCount() {
        return count;
    }

    public String getIp() {
        return ip;
    }
}
