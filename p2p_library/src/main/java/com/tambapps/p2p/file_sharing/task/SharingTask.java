package com.tambapps.p2p.file_sharing.task;

import com.tambapps.p2p.file_sharing.exception.SharingException;
import com.tambapps.p2p.file_sharing.TransferListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Task for sharing a file with another peer
 */
public abstract class SharingTask {

    public static final int MAX_PROGRESS = 100;
    protected final TransferListener transferListener;

    SharingTask(TransferListener transferListener) {
        this.transferListener = transferListener;
    }

    private volatile boolean canceled = false;

    void share(int bufferSize, InputStream is, OutputStream os, long totalBytes) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int count;
        int lastProgress = 0;
        long bytesProcessed = 0;
        int progress;
        while ((count = is.read(buffer)) > 0) {
            if (canceled) {
                return;
            }
            bytesProcessed += count;
            os.write(buffer, 0, count);
            progress = (int) Math.min(MAX_PROGRESS - 1, MAX_PROGRESS * bytesProcessed / totalBytes);
            if (progress != lastProgress) {
                lastProgress = progress;
                if (transferListener != null) {
                    transferListener.onProgressUpdate(progress, bytesProcessed, totalBytes);
                }
            }
        }
        if (bytesProcessed != totalBytes) {
            throw new SharingException("Transfer was not properly finished");
        } else if (transferListener != null) {
            transferListener.onProgressUpdate(MAX_PROGRESS, bytesProcessed, totalBytes);
        }
    }


    public void cancel() {
        canceled = true;
    }

    public TransferListener getTransferListener() {
        return transferListener;
    }

    public boolean isCanceled() {
        return canceled;
    }

}
