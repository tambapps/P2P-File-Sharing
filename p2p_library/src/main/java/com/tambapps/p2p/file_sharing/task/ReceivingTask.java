package com.tambapps.p2p.file_sharing.task;

import com.tambapps.p2p.file_sharing.Peer;
import com.tambapps.p2p.file_sharing.TransferListener;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class ReceivingTask extends SharingTask {

    private final FileProvider fileProvider;
    private volatile File outputFile = null;

    public ReceivingTask(File file) {
        this(file, null);
    }

    public ReceivingTask(FileProvider fileProvider) {
        this(fileProvider, null);
    }

    public ReceivingTask(File file, TransferListener transferListener) {
        this(name -> file, transferListener);
    }

    public ReceivingTask(FileProvider fileProvider, TransferListener transferListener) {
        super(transferListener);
        this.fileProvider = fileProvider;
    }

    public void receiveFrom(InetAddress address, int port) throws IOException {
        receiveFrom(Peer.of(address, port));
    }

    public void receiveFrom(Peer peer) throws IOException {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(new InetSocketAddress(peer.getIp(), peer.getPort()));
            while (!socketChannel.isConnected()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    cancel();
                    return;
                }
                if (isCanceled()) {
                    return;
                }
            }
            if (isCanceled()) {
                return;
            }
            try (Socket socket = socketChannel.socket();
                 DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                long totalBytes = dis.readLong();
                int bufferSize = dis.readInt();
                String fileName = readName(dis);
                if (transferListener != null) {
                    transferListener.onConnected(peer, Peer.of(socket.getInetAddress(), socket.getPort()),
                      fileName, totalBytes);
                }

                outputFile = fileProvider.newFile(fileName);
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    share(bufferSize, dis, fos, totalBytes);
                }
            }
        }

    }

    private String readName(DataInputStream dis) throws IOException {
        char[] name = new char[dis.readInt()];
        for (int i = 0; i < name.length; i++) {
            name[i] = dis.readChar();
        }
        return new String(name);
    }

    public File getOutputFile() {
        return outputFile;
    }

}
