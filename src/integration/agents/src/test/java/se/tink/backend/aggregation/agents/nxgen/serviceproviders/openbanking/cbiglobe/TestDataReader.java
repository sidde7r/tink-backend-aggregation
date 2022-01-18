package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/resources";

    public static final String CREATE_CONSENT_REQ = "createConsentRequest.json";
    public static final String CREATE_CONSENT_RESP = "createConsentResponse.json";

    public static final String FETCH_ACCOUNTS_OK = "fetchAccounts_ok.json";
    public static final String FETCH_ACCOUNTS_WITH_SOME_USELESS =
            "fetchAccounts_withSomeUseless.json";

    public static final String CONSENT_STATUS_REJECTED = "consentStatus_rejected.json";
    public static final String CONSENT_STATUS_VALID = "consentStatus_valid.json";

    public static final String CONSENT_DETAILS_DATE = "consentDetails_date.json";
    public static final String CONSENT_DETAILS_DATETIME = "consentDetails_datetime.json";
    public static final String CONSENT_DETAILS_UNEXPECTED = "consentDetails_unexpected.json";

    public static final String TOKEN = "token.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
