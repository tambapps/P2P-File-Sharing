package com.tambapps.p2p.fandem.cl.command.converter;

import com.beust.jcommander.ParameterException;

import java.io.File;

public class DirectoryFileConverter extends AbstractFileConverter {

  @Override
  protected void validate(File file) {
    if (!file.isDirectory()) {
      throw new ParameterException(String.format("%s isn't a directory", file.getName()));
    }
  }

}