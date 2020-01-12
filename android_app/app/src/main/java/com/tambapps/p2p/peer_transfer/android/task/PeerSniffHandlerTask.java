package com.tambapps.p2p.peer_transfer.android.task;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.tambapps.p2p.fandem.Peer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerSniffHandlerTask extends AsyncTask<Void, Void, Void> {

    public static final int PORT = 7999;
    private static final int SOCKET_BACKLOG = 50;

    private final Peer peer;
    private final String fileName;

    public PeerSniffHandlerTask(Peer peer, String fileName) {
        this.peer = peer;
        this.fileName = fileName;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.e("WIFI", "starting");
        try (ServerSocket serverSocket = new ServerSocket(PORT, SOCKET_BACKLOG, peer.getIp())) {
            Log.e("WIFI", "created" + serverSocket.getInetAddress());
            for (int i = 0; i < SOCKET_BACKLOG; i++) {
                // TODO move this into p2p library
                Socket socket = serverSocket.accept();
                Log.e("WIFI", "accepted");

                try (DataOutputStream dataInputStream = new DataOutputStream(socket.getOutputStream())) {
                    String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
                    dataInputStream.writeInt(deviceName.length());
                    dataInputStream.writeChars(deviceName);
                    dataInputStream.writeInt(peer.getPort());
                    dataInputStream.writeInt(fileName.length());
                    dataInputStream.writeChars(fileName);
                }
                Log.e("WIFI", "sent");

            }
        } catch (IOException e) {
            Log.e("PeerSniffHandlerTask", "Error while handling sniff: " + e.getMessage());
        }

        return null;
    }
}
