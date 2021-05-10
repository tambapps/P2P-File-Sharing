package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import com.tambapps.p2p.fandem.cl.command.converter.AddressConverter;
import com.tambapps.p2p.fandem.cl.command.converter.ExistingFileConverter;
import com.tambapps.p2p.speer.util.PeerUtils;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.Optional;

@Parameters(separators = "=", commandDescription = "Send file to another peer")
public class SendCommand {

    @Parameter(description = "path of the file to send", required = true,
        converter = ExistingFileConverter.class)
    private List<File> files;

    @Parameter(names = "-ip", description = "the ip used to send (optional)",
        converter = AddressConverter.class)
    private InetAddress ip = null;

    @Parameter(names = {"-p", "--port"}, description = "the port used to send (optional)")
    private Integer port;

    @Parameter(names = {"-t", "--timeout"}, description = "the port used to send (optional)")
    private int timeout = 90 * 1000;

    public List<File> getFiles() {
        return files;
    }

    public Optional<InetAddress> getIp() {
        return Optional.ofNullable(ip != null ? ip : PeerUtils.getIpAddressOrNull());
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    public int getTimeout() {
        return timeout;
    }
}
