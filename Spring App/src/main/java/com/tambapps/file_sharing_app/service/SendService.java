package com.tambapps.file_sharing_app.service;

import com.tambapps.file_sharing.FileSender;

import com.tambapps.file_sharing_app.FileSharingApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;


@Service
public class SendService extends FileService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SendService.class);

    @Value("${sender.address}")
    private String address;
    @Value("${socket.timeout}")
    private int socketTimeout; //in ms

    public SendService(ConcurrentMap<Integer, FileTask> progressMap,
                       ExecutorService executorService, Timer timer) {
        super(progressMap, executorService, timer);
    }


    public SendTask start(String filePath) throws IOException {
        SendTask task = new SendTask(filePath);
        task.execute();
        return task;
    }

    public class SendTask extends FileTask {
        private final FileSender fileSender;
        private final String filePath;

        SendTask(String filePath) throws IOException {
            fileSender = new FileSender(address, socketTimeout);
            fileSender.setConnectionListener(((remoteAddr, port, file) -> LOGGER.info("Starting transfer of {} to {}:{}", file.getName(), remoteAddr, port)));
            this.filePath = filePath;
        }

        public String getIp() {
            return fileSender.getIp();
        }

        public int getPort() {
            return fileSender.getPort();
        }

        @Override
        public void run() {
            LOGGER.info(String.format("Waiting for a client connection on %s:%d", fileSender.getIp(), fileSender.getPort()));
            try {
                fileSender.send(filePath);
                LOGGER.info("File successfully sent");
            } catch (SocketTimeoutException timeoutException) {
                LOGGER.warn("No connection was made to this serverSocket. Stopping this send task");
            } catch (IOException e) {
                LOGGER.error("An error occurred during sending task", e);
            }
        }

        @Override
        public int getProgress() {
            return fileSender.getProgress();
        }
    }

    public void manualSend(String filePath, String address) throws IOException {
        this.address = address;
        socketTimeout = 60000;
        new SendTask(filePath).run();
    }
}
