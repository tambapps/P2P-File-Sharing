package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.List;

@Parameters(separators = "=", commandDescription = "Send file to another peer")
public class SendCommand {

    @Parameter(description = "path of the file to send", required = true)
    private List<String> filePath;

    @Parameter(names = "-ip", description = "the ip used to send (optional)")
    private String ip = null;

    @Parameter(names = "-port", description = "the port used to send (optional)")
    private int port;

    public List<String> getFilePath() {
        return filePath;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
