package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;

public class AccountTestData {

    private static final String TEST_DATA_DIR =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/resources";

    public static AccountResponse getAccountResponse() {
        return deserializeFromFile("accounts.json", AccountResponse.class);
    }

    public static CardResponse getCardResponse() {
        return deserializeFromFile("cardAccount.json", CardResponse.class);
    }

    public static BalanceResponse getBalanceResponse() {
        return deserializeFromFile("accountBalances.json", BalanceResponse.class);
    }

    @SneakyThrows
    private static <T> T deserializeFromFile(String fileName, Class<T> tClass) {
        File file = Paths.get(TEST_DATA_DIR, fileName).toFile();
        return new ObjectMapper().readValue(file, tClass);
    }
}
