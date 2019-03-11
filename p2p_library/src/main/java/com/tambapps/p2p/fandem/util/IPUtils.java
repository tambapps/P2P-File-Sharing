package com.tambapps.p2p.fandem.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class IPUtils {

    /**
     * from https://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device-from-code
     * @return return the ip address of the device
     */
    public static InetAddress getIPAddress() throws SocketException {
        List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface intf : interfaces) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
                if (!addr.isLoopbackAddress()) {
                    if (addr.getHostAddress().indexOf(':')<0) { //is ipv4
                        return addr;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get an available port
     * @param inetAddress the address of the host
     * @return an available port
     */
    public static int getAvalaiblePort(InetAddress inetAddress) {
        int port = 8081;
        while (port < 65536) {
            try (ServerSocket serverSocket = new ServerSocket(port,0, inetAddress)) {
                //tests that port available
            } catch (IOException e) {
                port++;
                continue;
            }
            return port;
        }
        throw new RuntimeException("No available port was found");
    }

}