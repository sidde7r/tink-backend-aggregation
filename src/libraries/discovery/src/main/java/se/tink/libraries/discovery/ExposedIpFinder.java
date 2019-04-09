package se.tink.libraries.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

/**
 * The score is based on the amount of parts of the address that is identical
 *
 * <p>1.1.1.1 has a score of 1 compared to 1.2.2.2 1.1.1.1 has a score of 3 compared to 1.1.1.3
 *
 * <p>This scoring mechanism is not perfect as it does not take the subnet mask into account
 */
public class ExposedIpFinder {
    public static String getIpForRoute(Collection<InetAddress> addresses, String targetIP)
            throws UnknownHostException {
        final byte[] target = InetAddress.getByName(targetIP).getAddress();

        int maxScore = -1;

        // Use the first address by default
        String bestAddr = addresses.iterator().next().getHostAddress();

        for (InetAddress ip : addresses) {
            final byte[] addr = ip.getAddress();

            // Don't compare the addresses if they are of different size (IPv4 vs IPv6)
            if (target.length != addr.length) {
                continue;
            }

            int score = 0;

            for (int i = 0; i < addr.length; i++) {
                if (addr[i] == target[i]) {
                    score++;
                }
            }

            if (score > maxScore) {
                bestAddr = ip.getHostAddress();
                maxScore = score;
            }
        }

        return bestAddr;
    }
}
