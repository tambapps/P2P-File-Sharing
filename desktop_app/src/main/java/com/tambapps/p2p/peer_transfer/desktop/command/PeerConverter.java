package com.tambapps.p2p.peer_transfer.desktop.command;

import com.beust.jcommander.IStringConverter;
import com.tambapps.p2p.file_sharing.Peer;

public class PeerConverter implements IStringConverter<Peer> {
    @Override
    public Peer convert(String value) {
        return Peer.parse(value);
    }
}
