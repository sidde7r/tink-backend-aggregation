package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.util.UUID.randomUUID;

import com.sun.jersey.core.header.OutBoundHeaders;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConstants;

public class ConfigurationProvider {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String BASE_URL = "https://my.hypovereinsbank.de:443/mfp/api";

    String getBaseUrl() {
        return BASE_URL;
    }

    MultivaluedMap<String, Object> getStaticHeaders() {
        OutBoundHeaders headers = new OutBoundHeaders();
        headers.putSingle(HttpHeaders.ACCEPT_ENCODING, "br, gzip, deflat");
        headers.putSingle(
                HttpHeaders.USER_AGENT,
                "HVB Banking/4.1.0 (iPhone; iOS 12.4.3; Scale/2.00),HVB Banking/4.1.0 (iPhone; iOS 12.4.3; Scale/2.00),HVB Banking/4.1.0 (iPhone; iOS 12.4.3; Scale/2.00)/WLNativeAPI/8.0.0.00.2016-01-24T11:48:54Z");
        headers.putSingle(HttpHeaders.ACCEPT_LANGUAGE, "en-SE;q=1,en-SE;q=1,en");
        headers.putSingle("Connection", "keep-alive");
        headers.putSingle("X-Requested-With", "XMLHttpRequest");
        return headers;
    }

    String generateDeviceId() {
        return randomUUID().toString().toUpperCase();
    }

    String generateApplicationSessionId() {
        return String.format("%08x", RANDOM.nextInt());
    }

    KeyPair generateRsaKeyPair() {
        final KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        keyPairGenerator.initialize(WLConstants.RSA_KEY_SIZE);
        return keyPairGenerator.genKeyPair();
    }
}
