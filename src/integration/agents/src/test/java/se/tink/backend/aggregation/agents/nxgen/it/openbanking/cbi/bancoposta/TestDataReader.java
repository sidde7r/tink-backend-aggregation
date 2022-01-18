package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.bancoposta;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/cbi/bancoposta/resources";

    public static final String METHOD_SELECTION = "methodSelection.json";
    public static final String METHOD_SELECTION_NO_METHODS = "methodSelection_noMethods.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
