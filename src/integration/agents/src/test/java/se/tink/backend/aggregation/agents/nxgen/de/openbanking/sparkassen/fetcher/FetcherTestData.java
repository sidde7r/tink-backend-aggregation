package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class FetcherTestData {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/sparkassen/resources";

    static final FetchAccountsResponse NULL_ACCOUNTS =
            SerializationUtils.deserializeFromString(
                    "{\"accounts\": null}", FetchAccountsResponse.class);

    public static FetchBalancesResponse getFetchBalancesResponse(
            String currency, BigDecimal... amounts) {
        String json = "{\n" + "\t\"account\": null,\n" + "\t\"balances\": [";
        json +=
                Arrays.stream(amounts)
                        .map(amount -> getBalanceEntityJsonString(currency, amount))
                        .collect(Collectors.joining(","));
        json += "]}";
        return SerializationUtils.deserializeFromString(json, FetchBalancesResponse.class);
    }

    private static String getBalanceEntityJsonString(String currency, BigDecimal amount) {
        return "{\n"
                + "    \"balanceAmount\": {\n"
                + "        \"amount\": \""
                + amount
                + "\",\n"
                + "        \"currency\": \""
                + currency
                + "\"\n"
                + "    },\n"
                + "    \"balanceType\": \"ASDF\",\n"
                + "    \"referenceDate\": \"ASDF\"\n"
                + "}";
    }

    public static FetchAccountsResponse getFetchAccountsResponse(int correctAccounts) {
        String json = "{\"accounts\": [";
        json +=
                IntStream.rangeClosed(1, correctAccounts)
                        .mapToObj(
                                x ->
                                        getAccountEntityJsonString(
                                                "CACC",
                                                "EUR",
                                                "DE86999999990000001000",
                                                "Asdf",
                                                "Asdf",
                                                "Asdf",
                                                "Asdf"))
                        .collect(Collectors.joining(","));
        json += "]}";

        return SerializationUtils.deserializeFromString(json, FetchAccountsResponse.class);
    }

    public static AccountEntity getAccountEntity(
            String accountType,
            String currency,
            String iban,
            String name,
            String ownerName,
            String product,
            String resourceId) {
        return SerializationUtils.deserializeFromString(
                getAccountEntityJsonString(
                        accountType, currency, iban, name, ownerName, product, resourceId),
                AccountEntity.class);
    }

    private static String getAccountEntityJsonString(
            String accountType,
            String currency,
            String iban,
            String name,
            String ownerName,
            String product,
            String resourceId) {
        return "{\n"
                + "    \"cashAccountType\": \""
                + accountType
                + "\",\n"
                + "    \"currency\": \""
                + currency
                + "\",\n"
                + "    \"iban\": \""
                + iban
                + "\",\n"
                + "    \"name\": \""
                + name
                + "\",\n"
                + "    \"ownerName\": \""
                + ownerName
                + "\",\n"
                + "    \"product\": \""
                + product
                + "\",\n"
                + "    \"resourceId\": \""
                + resourceId
                + "\"\n"
                + "}";
    }

    public static String getTransactionsResponse() {
        return readAsStringFromFile("transactions.xml");
    }

    static String getTransactionsResponseWithNoTransactions() {
        return readAsStringFromFile("emptyTransactions.xml");
    }

    private static String readAsStringFromFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, filename)));
        } catch (IOException e) {
            return null;
        }
    }
}
