package com.tambapps.file_sharing;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSender {

    private static final int BUFFER_SIZE = 4096;

    private final ServerSocket serverSocket;
    private int progress;
    private TransferListener transferListener;

    public FileSender(InetAddress address, int socketTimeout) throws IOException {
        this(address, 0, socketTimeout);
    }

    public FileSender(String address, int socketTimeout) throws IOException {
        this(InetAddress.getByName(address), socketTimeout);
    }

    public FileSender(InetAddress address, int port, int socketTimeout) throws IOException {
        serverSocket = new ServerSocket(port, 1, address);
        serverSocket.setSoTimeout(socketTimeout);
    }

    public FileSender(String address, int port, int socketTimeout) throws IOException {
        this(InetAddress.getByName(address), port, socketTimeout);
    }

    public void send(File file) throws IOException {
        progress = 0;
        if (!file.exists()) {
            throw new IllegalArgumentException("The file with path " +
                    file.getPath() + " doesn't exist");
        } else if (!file.isFile()) {
            throw new IllegalArgumentException(file.getPath() + " isn't a file");
        }

        try (Socket socket = serverSocket.accept()) {
            if (transferListener != null) {
                transferListener.onConnected(socket.getRemoteSocketAddress().toString().substring(1),
                        socket.getPort());
            }
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 InputStream fis = new FileInputStream(file)) {
                long fileLength = file.length();
                long bytesSended = 0;
                dos.writeLong(fileLength);
                dos.writeInt(BUFFER_SIZE);
                dos.writeInt(file.getName().length());
                dos.writeChars(file.getName());
                int count;
                int lastProgress = 0;
                byte[] buffer = new byte[BUFFER_SIZE]; // or 4096, or more

                while ((count = fis.read(buffer)) > 0) {
                    bytesSended += count;
                    dos.write(buffer, 0, count);
                    progress = (int) Math.min(99, 100 * bytesSended / fileLength);
                    if (progress != lastProgress) {
                        lastProgress = progress;
                        if (transferListener != null) {
                            transferListener.onProgressUpdate(progress);
                        }
                    }
                }
                progress = 100;
                transferListener.onProgressUpdate(progress);
            }
            serverSocket.close();
        } finally {
            transferListener = null;
        }
    }

    public void send(String filePath) throws IOException {
        send(new File(filePath));
    }

    public void interrupt() throws IOException {
        serverSocket.close();
    }

    public void setTransferListener(TransferListener transferListener) {
        this.transferListener = transferListener;
    }

    public int getProgress() {
        return progress;
    }

    public String getIp() {
        String rawIp = serverSocket.getInetAddress().toString();
        int index = rawIp.indexOf("/");
        if (index < 0) {
            return rawIp;
        } else {
            return rawIp.substring(index + 1);
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

}
