package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.danskebank.fixtures;

import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Paths;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank.DanskeBankAccountTransactionsV31Response;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankResponseFixtures {

    static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/danskebank/fixtures/resources/";

    public static DanskeBankAccountTransactionsV31Response getAccountTransactionsV31Response() {
        return SerializationUtils.deserializeFromString(
                Paths.get(DATA_PATH, "transactionsResponse.json").toFile(),
                new TypeReference<DanskeBankAccountTransactionsV31Response>() {});
    }

    public static DanskeBankAccountTransactionsV31Response
            getNonZeroBalancingTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(DATA_PATH, "nonZeroBalancingTransactionsResponse.json").toFile(),
                new TypeReference<DanskeBankAccountTransactionsV31Response>() {});
    }

    public static DanskeBankAccountTransactionsV31Response getZeroBalancingTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(DATA_PATH, "zeroBalancingTransactionsResponse.json").toFile(),
                new TypeReference<DanskeBankAccountTransactionsV31Response>() {});
    }
}
