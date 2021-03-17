package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/mediolanum/resources";

    public static final String TWO_TRANSACTIONS = "twoTransactions.json";
    public static final String TWO_ACCOUNTS = "twoAccounts.json";
    public static final String UNAUTHORIZED = "unauthorized.json";
    public static final String TOKEN = "token.json";
    public static final String CONSENT_CREATED = "consentCreated.json";
    public static final String CONSENT_DETAILS_OK = "consentDetailsOk.json";
    public static final String CONSENT_DETAILS_NOT_OK = "consentDetailsNotOk.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
