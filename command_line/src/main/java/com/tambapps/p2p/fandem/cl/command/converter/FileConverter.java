package com.tambapps.p2p.fandem.cl.command.converter;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class FileConverter implements IStringConverter<File> {
  @Override
  public File convert(String filePath) {
    File file;
    try {
      file = new File(decodePath(filePath));
    } catch (UnsupportedEncodingException e) {
      throw new ParameterException("Unexpected error: " + e.getMessage());
    }
    if (!file.exists()) {
      throw new ParameterException(String.format("File with path '%s' doesn't exists", filePath));
    }
    if (!file.isFile()) {
      throw new ParameterException(String.format("%s isn't a file", filePath));
    }
    return file;
  }

  private String decodePath(String path) throws UnsupportedEncodingException {
    return URLDecoder.decode(path, StandardCharsets.UTF_8.name());
  }

}