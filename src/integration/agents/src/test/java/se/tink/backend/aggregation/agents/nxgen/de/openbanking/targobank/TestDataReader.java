package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataReader {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/targobank/resources";

    public static final String INCORRECT_CREDENTIALS = "targobank_incorrect_credentials.json";
    public static final String INCORRECT_CHALLENGE_RESPONSE =
            "targobank_incorrect_challenge_response.json";
    public static final String FIRST_PAYMENT_ONLINE_BANKING =
            "targobank_first_payment_online_banking.json";
    public static final String CONTACT_WITH_BANK = "targobank_payment_contact_with_bank.json";
    public static final String RECIPIENT_BANK_NOT_SUPPORTING_INSTANT_PAYMENTS =
            "targobank_payment_recipient_bank_not_supporting_instant_payments.json";

    public static <T> T readFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }
}
