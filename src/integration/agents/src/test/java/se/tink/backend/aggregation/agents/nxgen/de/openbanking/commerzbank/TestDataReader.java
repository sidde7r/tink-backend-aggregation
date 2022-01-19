package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/commerzbank/resources";

    public static final String PAYMENT_CREATED_OK_DECOUPLED = "payment_created_ok_decoupled.json";
    public static final String PAYMENT_CREATED_OK_REDIRECT = "payment_created_ok_redirect.json";
    public static final String SCA_STATUS_EXEMPTED = "sca_status_exempted.json";
    public static final String SCA_STATUS_FAILED = "sca_status_failed.json";
    public static final String SCA_STATUS_FINALISED = "sca_status_finalised.json";
    public static final String SCA_STATUS_STARTED = "sca_status_started.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
