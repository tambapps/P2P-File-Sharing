package com.tambapps.p2p.peer_transfer.desktop.service;

import com.tambapps.p2p.file_sharing.TransferListener;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class FileService {
  private final AtomicInteger idCount = new AtomicInteger();

  private ConcurrentMap<Integer, FileTask> progressMap;
  private ExecutorService executorService;
  private Timer timer;

  public FileService(ConcurrentMap<Integer, FileTask> progressMap,
                     ExecutorService executorService, Timer timer) {
    this.progressMap = progressMap;
    this.executorService = executorService;
    this.timer = timer;
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
        timer.schedule(new TimerTask() {
          @Override
          public void run() {
            progressMap.remove(id);
          }
        }, 10_000L);
      });
    }

    @Override
    public void onProgressUpdate(int progress) {

    }

    @Override
    public void onConnected(String remoteAddress, int port, String fileName) {

    }
  }
}
