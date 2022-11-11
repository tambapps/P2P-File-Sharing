package com.tambapps.p2p.fandem.cl.command.converter;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Converts {@link String} to {@link InetAddress}
 */
public class AddressConverter implements IStringConverter<InetAddress> {
  @Override
  public InetAddress convert(String value) {
    try {
      return InetAddress.getByName(value);
    } catch (UnknownHostException e) {
      throw new ParameterException("Couldn't get ip address (is it well formatted?)");
    }
  }
}
