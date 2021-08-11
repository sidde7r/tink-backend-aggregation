package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/resources";

    public static final String CONSENT_CREATED = "consentCreated.json";
    public static final String CONSENT_DETAILS_VALID = "consentDetailsValidConsentResponse.json";
    public static final String ONE_OFF_PAYMENT_REQUEST = "oneOffPaymentRequest.xml";
    public static final String RECURRING_PAYMENT = "recurringPayment.txt";
    public static final String PAYMENT_INITIALIZED = "paymentInitialized.json";

    public static final String SCA_FINALISED = "scaFinalised.json";
    public static final String SCA_RESPONSE_MULTIPLE = "scaResponseMultiple.json";
    public static final String SCA_RESPONSE_SELECTED = "scaResponseSelected.json";
    public static final String SCA_RESPONSE_SELECTED_CHIP_TAN = "scaResponseSelectedChipTan.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }

    public static String readFromFileAsString(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, filename)));
        } catch (IOException e) {
            return null;
        }
    }
}
