package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBConstants.RSA_KEY_SIZE;

import com.sun.jersey.core.header.OutBoundHeaders;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.ws.rs.core.MultivaluedMap;

public class ConfigurationProvider {

    private static final Random RANDOM = new Random();

    private static final String BASE_URL = "https://my.hypovereinsbank.de:443/mfp/api";

    public String getBaseUrl() {
        return BASE_URL;
    }

    public MultivaluedMap<String, Object> getStaticHeaders() {
        OutBoundHeaders headers = new OutBoundHeaders();
        headers.putSingle(ACCEPT_ENCODING, "br, gzip, deflat");
        headers.putSingle(
                USER_AGENT,
                "HVB Banking/4.1.0 (iPhone; iOS 12.4.3; Scale/2.00),HVB Banking/4.1.0 "
                        + "(iPhone; iOS 12.4.3; Scale/2.00),HVB Banking/4.1.0 (iPhone; iOS 12.4.3; Scale/2.00)/WLNativeAPI/8.0.0.00.2016-01-24T11:48:54Z");
        headers.putSingle(ACCEPT_LANGUAGE, "en-SE;q=1,en-SE;q=1,en");
        headers.putSingle("Connection", "keep-alive");
        return headers;
    }

    public String generateDeviceId() {
        return randomUUID().toString().toUpperCase();
    }

    public String generateApplicationSessionId() {
        return String.format("%08x", RANDOM.nextInt());
    }

    public KeyPair generateRsaKeyPair() {
        final KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        keyPairGenerator.initialize(RSA_KEY_SIZE);
        return keyPairGenerator.genKeyPair();
    }
}
