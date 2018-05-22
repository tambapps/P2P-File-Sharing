package com.tambapps.p2p.file_sharing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;

public class FileReceiver extends FileSharer {

    private final SocketChannel socketChannel;
    private final String downloadPath;
    private volatile File file;

    public FileReceiver(String downloadPath) throws IOException {
        this.downloadPath = downloadPath;
        socketChannel = SocketChannel.open();
    }

    public void receiveFrom(String peer) throws IOException {
        receiveFrom(peer.substring(0, peer.indexOf(':')),
                Integer.parseInt(peer.substring(peer.indexOf(':') + 1)));
    }

    public void receiveFrom(String address, int port) throws IOException {
        receiveFrom(InetAddress.getByName(address), port);
    }

    public void receiveFrom(InetAddress address, int port) throws IOException {
        init();
        file = null;

        socketChannel.connect(new InetSocketAddress(address, port));
        synchronized (this) {
            while (!isCanceled() && !socketChannel.isOpen()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Couldn't wait anymore", e);
                }
            }
        }

        Socket socket = socketChannel.socket();

        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            totalBytes = dis.readLong();
            int bufferSize = dis.readInt();
            String fileName = readName(dis);

            if (transferListener != null) {
                transferListener.onConnected(socket.getRemoteSocketAddress().toString().substring(1),
                        socket.getPort(), fileName);
            }

            file = newFile(downloadPath, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                boolean received = share(bufferSize, dis, fos);

                if (received && getBytesProcessed() != totalBytes) {
                    throw new TransferInterruptedException("Transfer was not properly finished");
                }

                progress = 100;
                if (transferListener != null) {
                    transferListener.onProgressUpdate(progress);
                }
            }

            socketChannel.close();
        } catch (AsynchronousCloseException e) {
            //task canceled by cancel() function
        } finally {
            transferListener = null;
        }
    }

    @Override
    void closeStream() throws IOException {
        socketChannel.close();
    }

    private String readName(DataInputStream dis) throws IOException {
        char[] name = new char[dis.readInt()];
        for (int i = 0; i < name.length; i++) {
            name[i] = dis.readChar();
        }
        return new String(name);
    }

    private File newFile(String directory, String name) throws IOException {
        File file = new File(directory, name);
        if (file.exists()) { //searching available file name
            for (int i = 0; file.exists() && i < 999; i++) {
                StringBuilder number = new StringBuilder(String.valueOf(i));
                while (number.length() < 3) {
                    number.insert(0, '0');
                }
                String fileName;
                if (name.contains(".")) {
                    int dotIndex = name.indexOf('.');
                    fileName = name.substring(0, dotIndex) + '_' + number + name.substring(dotIndex);
                } else {
                    fileName = name + '_' + number;
                }
                file = new File(directory, fileName);
            }
        }
        if (!file.createNewFile()) {
            throw new IOException("Couldn't create new file");
        }
        return file;
    }

    @Override
    public synchronized void cancel() {
        super.cancel();
        notifyAll();
    }

    public File getReceivedFile() {
        return file;
    }

    public long getBytesReceived() {
        return getBytesProcessed();
    }
}
