package com.tambapps.p2p.file_sharing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class FileSharer {

    private volatile boolean canceled = false;

    TransferListener transferListener;
    volatile int progress;
    private volatile long bytesProcessed;
    volatile long totalBytes;

    boolean share(int bufferSize, InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int count;
        int lastProgress = 0;
        long bytesProcessed = 0;
        while ((count = is.read(buffer)) > 0) {
            if (canceled) {
                return false;
            }
            bytesProcessed += count;
            this.bytesProcessed = bytesProcessed;
            os.write(buffer, 0, count);
            progress = (int) Math.min(99, 100 * bytesProcessed / totalBytes);
            if (progress != lastProgress) {
                lastProgress = progress;
                if (transferListener != null) {
                    transferListener.onProgressUpdate(progress, bytesProcessed, totalBytes);
                }
            }
        }
        progress = (int) (100 * bytesProcessed / totalBytes);
        transferListener.onProgressUpdate(progress, bytesProcessed, totalBytes);
        return true;
    }

    abstract void closeStream() throws IOException;

    public void cancel() {
        canceled = true;
        try {
            closeStream();
        } catch (IOException ignored) {

        }
    }

    void init() {
        progress = 0;
        bytesProcessed = 0;
        totalBytes = 0;
        canceled = false;
    }

    long getBytesProcessed() {
        return bytesProcessed;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTransferListener(TransferListener transferListener) {
        this.transferListener = transferListener;
    }

    public TransferListener getTransferListener() {
        return transferListener;
    }

    public int getProgress() {
        return progress;
    }

    public boolean isCanceled() {
        return canceled;
    }
}
