package com.tambapps.p2p.fandem;

import com.tambapps.p2p.fandem.exception.SharingException;
import com.tambapps.p2p.fandem.handshake.FandemHandshake;
import com.tambapps.p2p.fandem.util.TransferListener;
import com.tambapps.p2p.speer.handshake.Handshake;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@AllArgsConstructor
public abstract class FileSharer {

  private static final int MAX_PROGRESS = 100;

  protected final Handshake handshake;
  protected final TransferListener listener;

  protected void share(InputStream inputStream, OutputStream outputStream,
      long totalBytes) throws IOException {
    int bufferSize = 1024;
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
}
