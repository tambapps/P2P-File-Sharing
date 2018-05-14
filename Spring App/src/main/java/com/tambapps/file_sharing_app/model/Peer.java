package com.tambapps.file_sharing_app.model;

import javax.validation.constraints.*;

public class Peer {

    private final static String PORT_VALIDATION_ERROR = "The port is not valid";
    private final static String IP_VALIDATION_ERROR = "The ip is not valid";

    @Pattern(regexp = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)",
    message = IP_VALIDATION_ERROR)
    private String ip;

    @Min(value = 0, message = PORT_VALIDATION_ERROR)
    private int port;

    public Peer() {
    }
    public Peer(String peerString) {
        int index = peerString.indexOf(':');
        this.ip = peerString.substring(0, index);
        this.port = Integer.parseInt(peerString.substring(index + 1));
    }

    public Peer(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return ip+":"+port;
    }
}
