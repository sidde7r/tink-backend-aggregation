package se.tink.backend.utils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RemoteAddressUtils {
    public static String getRemoteAddress(SocketAddress socketAddress) {
        if (socketAddress == null) {
            return null;
        } else if (socketAddress instanceof InetSocketAddress) {
            return ((InetSocketAddress) socketAddress).getHostString();
        } else {
            return socketAddress.toString();
        }
    }
}
