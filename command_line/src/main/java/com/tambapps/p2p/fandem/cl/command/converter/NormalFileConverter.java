package com.tambapps.p2p.fandem.cl.command.converter;

import com.beust.jcommander.ParameterException;

import java.io.File;


/**
 * Converts {@link String} to {@link File}, checking that the file is a normal file
 */
public class NormalFileConverter extends ExistingFileConverter {

  @Override
  protected void validate(File file) {
    if (!file.isFile()) {
      throw new ParameterException("%s isn't a file".formatted(file.getName()));
    }
  }

}