package com.tambapps.p2p.peer_transfer.desktop.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(separators = "=", commandDescription = "Start Spring app accessible with your web browser")
public class SpringCommand {

    @Parameter(names = "--download.path", description = "path were received files will be downloaded", required = true)
    private String downloadPath;
}
