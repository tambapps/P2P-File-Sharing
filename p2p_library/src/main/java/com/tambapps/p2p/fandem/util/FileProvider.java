package com.tambapps.p2p.fandem.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public interface FileProvider {

  File newFile(String name) throws IOException;

  default OutputStreamProvider toOutputStreamProvider() {
    return (fileName) -> {
      File outputFile = newFile(fileName);
      if (!outputFile.exists() && !outputFile.createNewFile()) {
        throw new IOException("Couldn't create file " + outputFile);
      }
      return new FileOutputStream(outputFile);
    };
  }
}
