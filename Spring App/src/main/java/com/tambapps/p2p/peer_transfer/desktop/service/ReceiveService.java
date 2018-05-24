package com.tambapps.p2p.peer_transfer.desktop.service;

import com.tambapps.p2p.file_sharing.TransferInterruptedException;
import com.tambapps.p2p.peer_transfer.desktop.model.Peer;
import com.tambapps.p2p.file_sharing.FileReceiver;

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
                          ExecutorService executorService) {
        super(progressMap, executorService);
    }

    public ReceiveTask start(Peer peer) throws IOException {
        ReceiveTask receiveTask = new ReceiveTask(peer);
        receiveTask.execute();
        return receiveTask;
    }

    public class ReceiveTask extends FileTask {
        private final Peer peer;
        private FileReceiver fileReceiver;

        ReceiveTask(Peer peer) throws IOException {
            this.peer = peer;
            this.fileReceiver = new FileReceiver(downloadPath);
        }

        @Override
        public void run() {
            LOGGER.info("Connecting to host {}:{}", peer.getIp(), peer.getPort());
            try {
                fileReceiver.receiveFrom(peer.getIp(), peer.getPort());
                File file = fileReceiver.getReceivedFile();
                LOGGER.info("{} was successfully received", file.getName());
            } catch (TransferInterruptedException e) {
                LOGGER.error("Transfer was interrupted or not properly finished", e);
                File file = fileReceiver.getReceivedFile();
                if (file != null && file.exists() && !file.delete()) {
                    LOGGER.warn("The uncompleted file couldn't be delete, please do it yourself");
                }
            } catch (IOException e) {
                LOGGER.error("An error occurred during receive task", e);
            }
        }

        @Override
        public int getProgress() {
            return fileReceiver.getProgress();
        }
    }

    public void manualReceive(String downloadPath, String peer) throws IOException {
        this.downloadPath = downloadPath;
        new ReceiveTask(new Peer(peer)).run();
    }
}
