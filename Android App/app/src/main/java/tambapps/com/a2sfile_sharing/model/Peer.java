package tambapps.com.a2sfile_sharing.model;

/**
 * Created by fonkoua on 20/03/18.
 */

public class Peer {
    private String ip;
    private int port;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Peer(String ip, int port) {

        this.ip = ip;
        this.port = port;
    }
}
