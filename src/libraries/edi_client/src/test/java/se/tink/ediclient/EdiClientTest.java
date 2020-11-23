package se.tink.ediclient;

import org.junit.Ignore;
import org.junit.Test;

public class EdiClientTest {

    // Use this test to manually run the eIDAS dev issuer

    @Test
    @Ignore
    public void testIssue() throws Exception {
        EdiClient.requestOrGetDevCert();
    }
}
