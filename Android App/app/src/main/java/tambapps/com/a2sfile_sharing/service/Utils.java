package tambapps.com.a2sfile_sharing.service;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

/**
 * Created by fonkoua on 14/04/18.
 */

public class Utils {

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
}
