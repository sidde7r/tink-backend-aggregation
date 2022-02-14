package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.iccrea;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/iccrea/resources";

    public static final String CREDENTIALS_AUTHENTICATION = "credentialsAuthentication.json";
    public static final String CREDENTIALS_AUTHENTICATION_NO_PASSWORD =
            "credentialsAuthentication_noPassword.json";
    public static final String CREDENTIALS_AUTHENTICATION_NO_USERNAME =
            "credentialsAuthentication_noUsername.json";
    public static final String CREDENTIALS_AUTHENTICATION_NO_CREDENTIALS =
            "credentialsAuthentication_noCredentials.json";

    public static final String CONSENT_STATUS_RECEIVED = "consentStatus_received.json";
    public static final String CONSENT_STATUS_REJECTED = "consentStatus_rejected.json";
    public static final String CONSENT_STATUS_VALID = "consentStatus_valid.json";
    public static final String CONSENT_STATUS_CORRUPTED = "consentStatus_corrupted.json";

    public static final String METHOD_SELECTION = "methodSelection.json";
    public static final String METHOD_SELECTION_NO_METHODS = "methodSelection_noMethods.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
