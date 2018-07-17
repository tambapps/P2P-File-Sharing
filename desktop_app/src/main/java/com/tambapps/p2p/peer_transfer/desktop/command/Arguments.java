package com.tambapps.p2p.peer_transfer.desktop.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandNames = {"send", "receive"})
public class Arguments {

    @Parameter(names = "-help", help = true)
    private boolean help;

    public boolean getHelp() {
        return help;
    }
}
