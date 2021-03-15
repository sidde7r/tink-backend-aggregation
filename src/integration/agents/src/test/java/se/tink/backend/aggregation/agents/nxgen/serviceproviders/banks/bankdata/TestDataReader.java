package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bankdata/resources";

    public static final String ACCOUNTS_RESP = "accountsResponse.json";
    public static final String TRANSACTIONS_RESP = "accountsResponse.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
