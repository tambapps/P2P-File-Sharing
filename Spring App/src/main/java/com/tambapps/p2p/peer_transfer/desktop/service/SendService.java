package com.tambapps.p2p.peer_transfer.desktop.service;

import com.tambapps.p2p.file_sharing.FileSender;

import com.tambapps.p2p.file_sharing.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.Timer;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;


@Service
public class SendService extends FileService {

    private final static Logger LOGGER = LoggerFactory.getLogger(SendService.class);

    private final String address;
    @Value("${socket.timeout}")
    private int socketTimeout; //in ms

    public SendService(ConcurrentMap<Integer, FileTask> progressMap,
                       ExecutorService executorService, Timer timer) throws SocketException {
        super(progressMap, executorService, timer);
        address  = IPUtils.getIPAddress().getHostAddress();
    }

    public SendTask start(String filePath) throws IOException {
        SendTask task = new SendTask(filePath);
        task.execute();
        return task;
    }

    public class SendTask extends FileTask  {
        private final FileSender fileSender;
        private final String filePath;

        SendTask(String filePath) throws IOException {
            this(filePath, IPUtils.getAvalaiblePort(InetAddress.getByName(address)));
        }

        SendTask(String filePath, int port) throws IOException {
            fileSender = new FileSender(address, port, socketTimeout);
            fileSender.setTransferListener(this);
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

        @Override
        public void onConnected(String remoteAddr, int port, String fileName) {
            LOGGER.info("Starting transfer of {} to {}:{}", fileName, remoteAddr, port);
        }

    }
    public void manualSend(String filePath) throws IOException {
        manualSend(filePath, 0);
    }

    public void manualSend(String filePath, int port) throws IOException {
        socketTimeout = 60000;
        new SendTask(filePath, port).run();
    }
}
