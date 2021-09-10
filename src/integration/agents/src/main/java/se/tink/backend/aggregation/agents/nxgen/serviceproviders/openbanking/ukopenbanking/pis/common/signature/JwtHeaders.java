package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtHeaders {

    public static final String IAT_KEY_HEADER = "http://openbanking.org.uk/iat";
    public static final String ISS_KEY_HEADER = "http://openbanking.org.uk/iss";
    public static final String TAN_KEY_HEADER = "http://openbanking.org.uk/tan";
    public static final String CRIT_KEY_HEADER = "crit";
    static final String B64_KEY_HEADER = "b64";

    private final Map<String, Object> headers = new LinkedHashMap<>();

    public static JwtHeaders create() {
        return new JwtHeaders();
    }

    JwtHeaders addB64() {
        headers.put(B64_KEY_HEADER, false);
        return this;
    }

    public JwtHeaders addIat() {
        headers.put(IAT_KEY_HEADER, Instant.now().minusSeconds(3600).getEpochSecond());
        return this;
    }

    JwtHeaders addIatWithMillis(long millis) {
        headers.put(IAT_KEY_HEADER, millis);
        return this;
    }

    JwtHeaders addIssWithOrgId(String orgId, String softwareId) {
        addIss(String.format("%s/%s", orgId, softwareId));
        return this;
    }

    public JwtHeaders addIss(String id) {
        headers.put(ISS_KEY_HEADER, id);
        return this;
    }

    public JwtHeaders addTan(String tanHeaderValue) {
        headers.put(TAN_KEY_HEADER, tanHeaderValue);
        return this;
    }

    private void addCrit() {
        headers.put(CRIT_KEY_HEADER, ImmutableSet.copyOf(headers.keySet()));
    }

    public Map<String, Object> build() {
        addCrit();
        return headers;
    }
}
