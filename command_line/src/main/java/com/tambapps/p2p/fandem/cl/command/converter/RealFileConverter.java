package com.tambapps.p2p.fandem.cl.command.converter;

import com.beust.jcommander.ParameterException;

import java.io.File;

public class RealFileConverter extends ExistingFileConverter {

  @Override
  protected void validate(File file) {
    if (!file.isFile()) {
        throw new ParameterException(String.format("%s isn't a file", file.getName()));
    }
  }

}