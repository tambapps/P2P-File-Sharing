package com.tambapps.p2p.peer_transfer.desktop.command;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.file_sharing.Peer;

import java.net.UnknownHostException;

public class PeerConverter implements IStringConverter<Peer> {
    @Override
    public Peer convert(String value) {
        try {
            return Peer.parse(value);
        } catch (UnknownHostException e) {
            throw new ParameterException("Couldn't parse peer " + value);
        }
    }
}
