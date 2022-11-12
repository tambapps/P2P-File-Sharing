package com.tambapps.p2p.fandem.cl.command.converter;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;
import com.tambapps.p2p.fandem.Fandem;
import com.tambapps.p2p.speer.Peer;

/**
 * Converts {@link String} to {@link Peer}. Handles peer string or hex string.
 */
public class PeerConverter implements IStringConverter<Peer> {
  @Override
  public Peer convert(String value) {
    try {
      if (value.contains(".")) {
        return Peer.parse(value);
      } else {
        return Fandem.parsePeerFromHexString(value);
      }
    } catch (IllegalArgumentException e) {
      throw new ParameterException(e.getMessage(), e);
    }
  }
}
