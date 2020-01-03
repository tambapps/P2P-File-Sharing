package com.tambapps.p2p.fandem.cl.command;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Peer;

import java.net.UnknownHostException;

public class PeerConverter implements IStringConverter<Peer> {
    @Override
    public Peer convert(String value) {
        try {
            if (value.contains(".")) {
                return Peer.parse(value);
            } else {
                return Peer.fromHexString(value);
            }
        } catch (UnknownHostException e) {
            throw new ParameterException("Couldn't parse peer " + value);
        }
    }
}
