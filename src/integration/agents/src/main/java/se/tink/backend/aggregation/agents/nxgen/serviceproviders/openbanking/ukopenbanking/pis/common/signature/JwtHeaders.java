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
    private static final String TINK_UK_OPEN_BANKING_ORG_ID = "00158000016i44IAAQ";
    private static final String GENERAL_STANDARD_ISS = "1f1YEdOMw6AphlVC6k2JQR";
    private static final String TESCO_SPECIAL_ISS = "uv8UDbYNKLtaWWZcGEcMoF";
    private static final String UKOB_TAN = "openbanking.org.uk";
    private static final String RFC_2253_DN =
            "CN=00158000016i44IAAQ, OID.2.5.4.97=PSDSE-FINA-44059, O=Tink AB, C=GB";

    private final Map<String, Object> headers = new LinkedHashMap<>();

    public static JwtHeaders create() {
        return new JwtHeaders();
    }

    public JwtHeaders addB64() {
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

    JwtHeaders addIssWithTinkOrgId() {
        addIss(String.format("%s/%s", TINK_UK_OPEN_BANKING_ORG_ID, GENERAL_STANDARD_ISS));
        return this;
    }

    public JwtHeaders addTescoSpecialIss() {
        addIss(String.format("%s/%s", TINK_UK_OPEN_BANKING_ORG_ID, TESCO_SPECIAL_ISS));
        return this;
    }

    public JwtHeaders addIssWithRfcDn() {
        addIss(RFC_2253_DN);
        return this;
    }

    JwtHeaders addIss(String id) {
        headers.put(ISS_KEY_HEADER, id);
        return this;
    }

    public JwtHeaders addTan() {
        headers.put(TAN_KEY_HEADER, UKOB_TAN);
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
