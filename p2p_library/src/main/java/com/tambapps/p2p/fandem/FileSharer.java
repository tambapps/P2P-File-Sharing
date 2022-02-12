package com.tambapps.p2p.fandem;

import static com.tambapps.p2p.fandem.util.FileUtils.bytesToHex;

import com.tambapps.p2p.fandem.exception.CorruptedFileException;
import com.tambapps.p2p.fandem.exception.SharingException;
import com.tambapps.p2p.fandem.util.FileUtils;
import com.tambapps.p2p.fandem.util.TransferListener;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
public abstract class FileSharer {

  public static final int DEFAULT_BUFFER_SIZE = 8192;

  private static final int MAX_PROGRESS = 100;

  protected final TransferListener listener;

  protected void share(InputStream inputStream, OutputStream outputStream, int bufferSize,
      // nullable expectedChecksum
      long totalBytes, String expectedChecksum) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int lastProgress = 0;
    long bytesProcessed = 0;
    int progress;
    int count;
    MessageDigest digest = FileUtils.getSha256MessageDigest();
    while ((count = inputStream.read(buffer, 0,
        totalBytes - bytesProcessed > buffer.length ? buffer.length : (int) (totalBytes - bytesProcessed))) > 0) {
      bytesProcessed += count;
      outputStream.write(buffer, 0, count);
      digest.update(buffer, 0, count);
      progress = (int) Math.min(MAX_PROGRESS - 1, MAX_PROGRESS * bytesProcessed / totalBytes);
      if (progress != lastProgress && listener != null) {
        lastProgress = progress;
        listener.onProgressUpdate(progress, bytesProcessed, totalBytes);
      }
    }
    if (bytesProcessed != totalBytes) {
      throw new SharingException("Transfer was not properly finished");
    } else if (listener != null) {
      listener.onProgressUpdate(MAX_PROGRESS, totalBytes, totalBytes);
    }
    if (expectedChecksum != null && !bytesToHex(digest.digest()).equals(expectedChecksum)) {
      throw new CorruptedFileException();
    }
  }
}
