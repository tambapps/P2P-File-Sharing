package com.tambapps.p2p.fandem.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

/**
 * Exception thrown when a checksum check fail
 */
@AllArgsConstructor
@Getter
public class CorruptedFileException extends IOException {

  private final File outputFile;

}
