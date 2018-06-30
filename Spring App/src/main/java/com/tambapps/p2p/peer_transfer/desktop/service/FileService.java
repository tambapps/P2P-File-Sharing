package com.tambapps.p2p.peer_transfer.desktop.service;

import com.tambapps.p2p.file_sharing.TransferListener;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class FileService {
  private final AtomicInteger idCount = new AtomicInteger();

  private ConcurrentMap<Integer, FileTask> progressMap;
  private ExecutorService executorService;

  public FileService(ConcurrentMap<Integer, FileTask> progressMap,
                     ExecutorService executorService) {
    this.progressMap = progressMap;
    this.executorService = executorService;
  }

  public abstract class FileTask implements TransferListener {
    public final int id;
    FileTask() {
      id = idCount.getAndIncrement();
    }
    abstract void run();
    public abstract int getProgress();

    void execute() {
      executorService.execute(() -> {
        progressMap.put(id, this);
        run();
      });
    }

    @Override
    public void onProgressUpdate(int progress, long byteProcessed, long totalBytes) {

    }

    @Override
    public void onConnected(String remoteAddress, int port, String fileName, long fileSize) {

    }
  }
}
