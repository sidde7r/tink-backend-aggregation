package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.Ignore;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class TestDataUtils {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bankdata/resources";

    public static final String ACCOUNTS_RESP = "accountsResponse.json";
    public static final String TRANSACTIONS_RESP = "accountsResponse.json";

    public static <T> T readDataFromFile(String filename, Class<T> klass) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), klass);
    }

    public static void verifyIdentifiers(
            Account account, Map<AccountIdentifierType, String> expectedIdentifiers) {
        Map<AccountIdentifierType, String> actualIdentifiers =
                account.getIdentifiers().stream()
                        .collect(
                                Collectors.toMap(
                                        AccountIdentifier::getType,
                                        AccountIdentifier::getIdentifier));
        assertThat(actualIdentifiers).containsExactlyInAnyOrderEntriesOf(expectedIdentifiers);
    }
}
