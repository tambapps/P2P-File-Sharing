package com.tambapps.p2p.fandem.cl.command.converter;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Converts {@link String} to {@link File}, checking that the file/directory exists
 */
public class ExistingFileConverter implements IStringConverter<File> {

  @Override
  public File convert(String filePath) {
    File file;
    try {
      file = new File(decodePath(filePath));
    } catch (UnsupportedEncodingException e) {
      throw new ParameterException("Unexpected error: " + e.getMessage());
    }
    if (!file.exists()) {
      throw new ParameterException("File with path '%s' doesn't exists".formatted(filePath));
    }
    validate(file);
    return file;
  }

  // overridable
  protected void validate(File file) {
  }

  private String decodePath(String path) throws UnsupportedEncodingException {
    return URLDecoder.decode(path, StandardCharsets.UTF_8);
  }

}