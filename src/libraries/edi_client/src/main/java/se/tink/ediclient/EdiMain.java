package se.tink.ediclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.eidasdevissuer.client.EdiClient;

public class EdiMain {
    private static final Logger LOG = LoggerFactory.getLogger(EdiMain.class);

    public static void main(String[] args) {
        try {
            EdiClient.requestOrGetDevCert();
            LOG.info("Successfully issued certificate");
        } catch (Exception ex) {
            LOG.error("Exception running eIDAS dev issuer", ex);
        }
    }
}
