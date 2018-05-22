package com.tambapps.p2p.file_sharing;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class FileSender extends FileSharer {

    private static final int BUFFER_SIZE = 4096;

    private final ServerSocket serverSocket;

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

    public void send(String filePath) throws IOException {
        send(new File(filePath));
    }

    public void send(File file) throws IOException {
        if (!file.exists()) {
            throw new IllegalArgumentException("The file with path " +
                    file.getPath() + " doesn't exist");
        } else if (!file.isFile()) {
            throw new IllegalArgumentException(file.getPath() + " isn't a file");
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        send(fileInputStream, file.getName(), file.length());
        fileInputStream.close();

    }

    public void send(InputStream fis, String fileName,
                     long fileSize) throws IOException {
        init();
        totalBytes = fileSize;

        try (Socket socket = serverSocket.accept()) {
            if (transferListener != null) {
                transferListener.onConnected(socket.getRemoteSocketAddress().toString().substring(1),
                        socket.getPort(), fileName);
            }
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                dos.writeLong(fileSize);
                dos.writeInt(BUFFER_SIZE);
                dos.writeInt(fileName.length());
                dos.writeChars(fileName);

                if (share(BUFFER_SIZE, fis, dos)) {
                    progress = 100;
                    if (transferListener != null) {
                        transferListener.onProgressUpdate(progress);
                    }
                }
            }
            serverSocket.close();
        } catch (SocketException e) {
            //socket closed because of cancel()
        } finally {
            transferListener = null;
        }
    }

    @Override
    void closeSocket() throws IOException {
        serverSocket.close();
    }

    public void setTransferListener(TransferListener transferListener) {
        this.transferListener = transferListener;
    }

    public int getProgress() {
        return progress;
    }

    public String getIp() {
        return serverSocket.getInetAddress().getHostAddress();
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public long getBytesSent() {
        return getBytesProcessed();
    }
}
