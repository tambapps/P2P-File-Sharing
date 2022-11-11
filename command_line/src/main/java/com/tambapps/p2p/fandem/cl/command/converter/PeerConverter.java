package com.tambapps.p2p.fandem.cl.command.converter;

import com.beust.jcommander.IStringConverter;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.speer.Peer;

/**
 * Converts {@link String} to {@link Peer}. Handles peer string or hex string.
 */
public class PeerConverter implements IStringConverter<Peer> {
  @Override
  public Peer convert(String value) {
    if (value.contains(".")) {
      return Peer.parse(value);
    } else {
      return Fandem.parsePeerFromHexString(value);
    }
  }
}
