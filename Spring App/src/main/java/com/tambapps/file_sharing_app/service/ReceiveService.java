package com.tambapps.file_sharing_app.service;

import com.tambapps.file_sharing_app.model.Peer;
import com.tambapps.file_sharing.FileReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Service
public class ReceiveService extends FileService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ReceiveService.class);

    @Value("${download.path}")
    private String downloadPath;

    public ReceiveService(ConcurrentMap<Integer, FileTask> progressMap,
                          ExecutorService executorService, Timer timer) {
        super(progressMap, executorService, timer);
    }

    public ReceiveTask start(Peer peer) {
        ReceiveTask receiveTask = new ReceiveTask(peer);
        receiveTask.execute();
        return receiveTask;
    }

    public class ReceiveTask extends FileTask {
        private final Peer peer;
        private FileReceiver fileReceiver;

        ReceiveTask(Peer peer) {
            this.peer = peer;
            this.fileReceiver = new FileReceiver(downloadPath);
        }

        @Override
        public void run() {
            LOGGER.info("Connecting to host {}:{}", peer.getIp(), peer.getPort());
            try {
                File file = fileReceiver.receiveFrom(peer.getIp(), peer.getPort());
                LOGGER.info("{} was successfully received", file.getName());
            } catch (IOException e) {
                LOGGER.error("An error occurred during receive task", e);
            }
        }

        @Override
        public int getProgress() {
            return fileReceiver.getProgress();
        }
    }

    public void manualReceive(String downloadPath, String peer) {
        this.downloadPath = downloadPath;
        new ReceiveTask(new Peer(peer)).run();
    }
}
