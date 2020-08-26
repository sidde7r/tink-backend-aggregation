package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_LANGUAGE;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBConstants.PREDEFINED_RSA_KEY_PAIR;

import com.sun.jersey.core.header.OutBoundHeaders;
import java.security.KeyPair;
import javax.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class ConfigurationProvider {

    private static final String BASE_URL = "https://my.hypovereinsbank.de:443/mfp/api";

    private final RandomValueGenerator randomValueGenerator;

    public String getBaseUrl() {
        return BASE_URL;
    }

    public MultivaluedMap<String, Object> getStaticHeaders() {
        OutBoundHeaders headers = new OutBoundHeaders();
        headers.putSingle(ACCEPT_ENCODING, "br, gzip, deflat");
        headers.putSingle(
                USER_AGENT,
                "HVB Banking/4.2.3 (iPhone; iOS 13.5.1; Scale/3.00),HVB Banking/4.2.3 "
                        + "(iPhone; iOS 13.5.1; Scale/3.00),HVB Banking/4.2.3 (iPhone; iOS 13.5.1; Scale/3.00)/WLNativeAPI/8.0.0.00.2016-01-24T11:48:54Z");
        headers.putSingle(ACCEPT_LANGUAGE, "en-SE;q=1,en-SE;q=1,en");
        headers.putSingle("Connection", "keep-alive");
        return headers;
    }

    public String generateDeviceId() {
        return randomValueGenerator.getUUID().toString().toUpperCase();
    }

    public String generateApplicationSessionId() {
        return String.format("%08x", randomValueGenerator.randomInt(Integer.MAX_VALUE));
    }

    public KeyPair getPredefinedRsaKeyPair() {
        return SerializationUtils.deserializeKeyPair(PREDEFINED_RSA_KEY_PAIR);
    }
}
