package com.tambapps.p2p.fandem.exception;

import java.io.File;
import java.io.IOException;

/**
 * util IO Exception thrown AFTER the output file was created
 */
public class ReceiverIOException extends IOException {

  private final File outputFile;

  public ReceiverIOException(Throwable cause, File outputFile) {
    super(cause);
    this.outputFile = outputFile;
  }
}
