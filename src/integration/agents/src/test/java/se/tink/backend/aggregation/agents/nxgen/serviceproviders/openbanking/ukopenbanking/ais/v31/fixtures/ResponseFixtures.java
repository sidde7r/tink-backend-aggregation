package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fixtures;

import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Paths;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ResponseFixtures {

    static final String DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/v31/fixtures/resources/";

    public static AccountTransactionsV31Response getAccountTransactionsV31Response() {
        return SerializationUtils.deserializeFromString(
                Paths.get(DATA_PATH, "transactionsResponse.json").toFile(),
                new TypeReference<AccountTransactionsV31Response>() {});
    }

    public static AccountTransactionsV31Response getNonRejectedTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(DATA_PATH, "nonRejectedTransactionsResponse.json").toFile(),
                new TypeReference<AccountTransactionsV31Response>() {});
    }

    public static AccountTransactionsV31Response getRejectedTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(DATA_PATH, "rejectedTransactionsResponse.json").toFile(),
                new TypeReference<AccountTransactionsV31Response>() {});
    }
}
