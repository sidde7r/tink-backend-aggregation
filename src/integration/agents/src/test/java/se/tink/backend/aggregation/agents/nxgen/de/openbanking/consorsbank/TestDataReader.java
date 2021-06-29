package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/consorsbank/resources";

    public static final String TRANSACTIONS_WITH_NEXT = "transactionsWithNext.json";
    public static final String TRANSACTIONS_WITHOUT_NEXT = "transactionsWithoutNext.json";
    public static final String TRANSACTIONS_BOTH_KINDS = "transactionsBothKinds.json";
    public static final String TWO_ACCOUNTS = "twoAccounts.json";
    public static final String BALANCES = "balances.json";

    public static final String CONSENT_REQUEST = "consentRequest.json";
    public static final String CONSENT_CREATED = "consentCreated.json";
    public static final String CONSENT_DETAILS_OK = "consentDetailsOk.json";
    public static final String CONSENT_DETAILS_NOT_OK = "consentDetailsNotOk.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
