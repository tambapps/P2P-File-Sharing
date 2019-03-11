package com.tambapps.p2p.file_sharing.task;

import com.tambapps.p2p.file_sharing.Peer;
import com.tambapps.p2p.file_sharing.TransferListener;
import com.tambapps.p2p.file_sharing.util.FileUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * A P2P Sending Task
 */
public class SendingTask extends SharingTask {

    public static final int BUFFER_SIZE = 4096;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10000;

    private final Peer peer;
    private final int bufferSize;
    private final int socketTimeout;
    private ServerSocket serverSocket; //as class variable to allow cancel

    public SendingTask(InetAddress address) {
        this(address, 0, 0);
    }

    public SendingTask(InetAddress address, int port, int socketTimeout) {
        this(address, port, socketTimeout, BUFFER_SIZE);
    }

    public SendingTask(InetAddress address, int port, int socketTimeout, int bufferSize) {
        this(null, Peer.of(address, port), socketTimeout, bufferSize);
    }

    public SendingTask(TransferListener transferListener, InetAddress address,
                       int port, int socketTimeout, int bufferSize) {
        this(transferListener, Peer.of(address, port), socketTimeout, bufferSize);
    }

    public SendingTask(TransferListener transferListener, Peer peer,
                       int socketTimeout, int bufferSize) {
        super(transferListener);
        this.peer = peer;
        this.bufferSize = bufferSize;
        this.socketTimeout = socketTimeout;
    }

    public SendingTask(Peer senderPeer, int socketTimeout) {
        this(senderPeer.getIp(), senderPeer.getPort(), socketTimeout);
    }

    public SendingTask(Peer senderPeer) {
        this(senderPeer.getIp(), senderPeer.getPort(), DEFAULT_SOCKET_TIMEOUT);
    }

    public void send(String filePath) throws IOException {
        send(new File(FileUtils.decodePath(filePath)));
    }

    public void send(File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            send(fileInputStream, file.getName(), file.length());
        }
    }

    public void send(InputStream fis, String fileName,
                     long fileSize) throws IOException {
        try (ServerSocket serverSocket = createServerSocket();
          Socket socket = serverSocket.accept()) {
            if (transferListener != null) {
                transferListener.onConnected(peer, Peer.of(socket), fileName, fileSize);
            }
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                dos.writeLong(fileSize);
                dos.writeInt(bufferSize);
                dos.writeInt(fileName.length());
                dos.writeChars(fileName);
                share(bufferSize, fis, dos, fileSize);
            }
        } catch (SocketException e) {
            //socket closed because of cancel()
        }
    }

    private ServerSocket createServerSocket() throws IOException {
        serverSocket = new ServerSocket(peer.getPort(), 1, peer.getIp());
        serverSocket.setSoTimeout(socketTimeout);
        return serverSocket;
    }

    @Override
    public void cancel() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {

            } finally {
                serverSocket = null;
            }
        }
        super.cancel();
    }
}
