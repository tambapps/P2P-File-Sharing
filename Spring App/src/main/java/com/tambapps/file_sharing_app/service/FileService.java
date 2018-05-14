package com.tambapps.file_sharing_app.service;

import org.springframework.beans.factory.annotation.Autowired;

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

  public abstract class FileTask {
    public final int id; //public to be accessible by javascript scripts
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
        }, 1000L);
      });
    }
  }
}
