package se.tink.libraries.discovery;

import static junit.framework.TestCase.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ExposedIpFinderTest {

    @Test
    public void testIpRanking() throws UnknownHostException {
        Collection<InetAddress> availableAddresses =
                new HashSet<>(
                        Arrays.asList(
                                InetAddress.getByName("127.0.0.1"),
                                InetAddress.getByName("10.1.3.7"),
                                InetAddress.getByName("10.200.3.7"),
                                InetAddress.getByName("172.17.0.1")));

        assertEquals("10.1.3.7", ExposedIpFinder.getIpForRoute(availableAddresses, "10.1.200.6"));
    }

    @Test
    public void testIpRankingOtherOrder() throws UnknownHostException {
        Collection<InetAddress> availableAddresses =
                new HashSet<>(
                        Arrays.asList(
                                InetAddress.getByName("10.200.3.7"),
                                InetAddress.getByName("172.17.0.1"),
                                InetAddress.getByName("127.0.0.1"),
                                InetAddress.getByName("10.1.3.7")));

        assertEquals("10.1.3.7", ExposedIpFinder.getIpForRoute(availableAddresses, "10.1.200.6"));
    }

    @Test
    public void testIpRankingIPv6() throws UnknownHostException {
        Collection<InetAddress> availableAddresses =
                new HashSet<>(
                        Arrays.asList(
                                InetAddress.getByName("127.0.0.1"),
                                InetAddress.getByName("10.1.3.7"),
                                InetAddress.getByName("172.17.0.1"),
                                InetAddress.getByName("2001:1111:0000:0000:0000:ff00:0042:8329"),
                                InetAddress.getByName("2001:8888:0000:0000:0000:ff00:0042:8329")));

        assertEquals(
                "2001:1111:0:0:0:ff00:42:8329",
                ExposedIpFinder.getIpForRoute(
                        availableAddresses, "2001:1111:0000:0000:0000:ff00:1111:8329"));
    }
}
