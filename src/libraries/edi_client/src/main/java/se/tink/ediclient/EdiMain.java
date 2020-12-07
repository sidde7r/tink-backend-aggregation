package se.tink.ediclient;

import org.slf4j.Logger;

public class EdiMain {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(EdiMain.class);

    public static void main(String[] args) {
        try {
            EdiClient.requestOrGetDevCert();
            LOG.info("Successfully issued certificate");
        } catch (Exception ex) {
            LOG.error("Exception running eIDAS dev issuer", ex);
        }
    }
}
