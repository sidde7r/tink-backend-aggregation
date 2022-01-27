package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fintecsystems/resources";

    public static final String REPORT = "report.json";
    public static final String CREATE_OK = "createOk.json";
    public static final String SESSION_OK = "sessionOk.json";
    public static final String PAYMENT_OK = "paymentOk.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
