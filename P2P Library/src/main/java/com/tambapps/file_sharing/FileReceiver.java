package com.tambapps.file_sharing;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class FileReceiver {

    private final String downloadPath;
    private volatile int progress;
    private volatile long bytesReceived;
    private volatile long totalBytes;
    private Socket socket;
    private File outputFile;
    private TransferListener transferListener;

    public FileReceiver(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public File receiveFrom(String peer) throws IOException {
        return receiveFrom(peer.substring(0, peer.indexOf(':')),
                Integer.parseInt(peer.substring(peer.indexOf(':') + 1)));
    }

    public File receiveFrom(String address, int port) throws IOException {
        return receiveFrom(InetAddress.getByName(address), port);
    }

    public File receiveFrom(InetAddress address, int port) throws IOException {
        progress = 0;
        bytesReceived= 0;

        this.outputFile = null;
        File outputFile;
        try (Socket socket = new Socket(address, port);
             DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            this.socket = socket;

            totalBytes = dis.readLong();
            long bytesRead = 0L;
            int bufferSize = dis.readInt();
            String fileName = readName(dis);

            if (transferListener != null) {
                transferListener.onConnected(socket.getRemoteSocketAddress().toString().substring(1),
                        socket.getPort(), fileName);
            }

            outputFile = newFile(downloadPath, fileName);
            int lastProgress = 0;
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[bufferSize];
                int count;
                while ((count = dis.read(buffer)) > 0) {
                    bytesRead += count;
                    this.bytesReceived = bytesRead;
                    fos.write(buffer, 0, count);
                    progress = (int) Math.min(99, 100 * bytesRead / totalBytes);
                    if (progress != lastProgress) {
                        lastProgress = progress;
                        if (transferListener != null) {
                            transferListener.onProgressUpdate(progress);
                        }
                    }
                }

                if (bytesRead != totalBytes) {
                    throw new TransferInterruptedException("Transfer was not properly finished", outputFile);
                }

                progress = 100;
                if (transferListener != null) {
                    transferListener.onProgressUpdate(progress);
                }
            }
        } finally {
            this.socket = null;
            transferListener = null;
        }
        return outputFile;
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

    public int getProgress() {
        return progress;
    }

    public File interrupt() throws IOException {
        if (socket != null) {
            socket.close();
            socket = null;
        }

        return outputFile;
    }

    public void setTransferListener(TransferListener transferListener) {
        this.transferListener = transferListener;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}
