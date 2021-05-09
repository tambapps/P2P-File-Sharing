package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.exception.SharingException;
import com.tambapps.p2p.fandem.handshake.FandemHandshake;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.handshake.Handshake;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@AllArgsConstructor
public abstract class FileSharer {

  private static final int MAX_PROGRESS = 100;

  protected final Handshake handshake;
  protected final TransferListener listener;

  protected void share(InputStream inputStream, OutputStream outputStream, int bufferSize,
      long totalBytes) throws IOException {
    byte[] buffer = new byte[bufferSize];
    int lastProgress = 0;
    long bytesProcessed = 0;
    int progress;
    int count;
    while ((count = inputStream.read(buffer)) > 0) {
      bytesProcessed += count;
      outputStream.write(buffer, 0, count);
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
  }

  public String computeChecksum(File file) throws IOException {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      return computeChecksum(inputStream);
    }
  }

  public String computeChecksum(InputStream inputStream) throws IOException {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("Couldn't find MD5 algorithm", e);
    }
    try (DigestInputStream dis = new DigestInputStream(inputStream, md)) {
      return new String(md.digest());
    }
  }
}
