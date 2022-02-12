package com.tambapps.p2p.fandem.model;

import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.IoSupplier;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SendingFileData extends FileData {

  // transient because we don't want this serialized when passing them in handshake
  private transient final IoSupplier<InputStream> inputStreamIoSupplier;

  public SendingFileData(String fileName, long fileSize,
      IoSupplier<InputStream> inputStreamIoSupplier) {
    this(fileName, fileSize, null, inputStreamIoSupplier);
  }
  public SendingFileData(String fileName, long fileSize, String checksum,
      IoSupplier<InputStream> inputStreamIoSupplier) {
    super(fileName, fileSize, checksum);
    this.inputStreamIoSupplier = inputStreamIoSupplier;
  }

  public InputStream newInputStream() throws IOException {
    return inputStreamIoSupplier.get();
  }

  public static SendingFileData fromFile(File file) throws IOException {
    return fromFile(file, true);
  }

  public static SendingFileData fromFile(File file, boolean withChecksum) throws IOException {
    String checksum;
    if (withChecksum) {
      checksum = FileUtils.computeChecksum(file);
    } else {
      checksum = null;
    }
    return new SendingFileData(file.getName(), file.length(), checksum, () -> new FileInputStream(file));
  }
}
