package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.data;

import java.nio.file.Paths;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.rpc.TransactionsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class NorwegianFetcherTestData {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/norwegian/resources";

    // ===========================TRANSACTIONS==========================================
    // booked
    public static final String TRANSACTION_0_DESCRIPTION = "TRANSACTION_0_DESCRIPTION";
    public static final String TRANSACTION_1_DESCRIPTION = "TRANSACTION_1_DESCRIPTION";
    public static final String TRANSACTION_2_DESCRIPTION = "TRANSACTION_2_DESCRIPTION";
    public static final String TRANSACTION_0_AMOUNT = "-75";
    public static final String TRANSACTION_1_AMOUNT = "40";
    public static final String TRANSACTION_2_AMOUNT = "-161.31";
    // pending
    public static final String TRANSACTION_3_DESCRIPTION = "TRANSACTION_3_DESCRIPTION";
    public static final String TRANSACTION_4_DESCRIPTION = "TRANSACTION_4_DESCRIPTION";
    public static final String TRANSACTION_3_AMOUNT = "300";
    public static final String TRANSACTION_4_AMOUNT = "-3563.58";
    // ===========================ACCOUNTS==============================================
    public static final String ACCOUNT_1_RESOURCE_ID = "someResourceId";
    public static final String ACCOUNT_1_BBAN = "01234567890";
    public static final String ACCOUNT_1_NAME = "SavingsAccount";
    public static final String ACCOUNT_2_RESOURCE_ID = "anotherResourceId";
    public static final String ACCOUNT_2_BBAN = "98745632145";
    public static final String ACCOUNT_2_NAME = "CreditCard";
    // ===========================BALANCES==============================================
    public static final String BALANCE_1 = "3000.01";
    public static final String BALANCE_2 = "12314.01";

    public static AccountsResponse getAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "accounts.json").toFile(), AccountsResponse.class);
    }

    public static BalanceResponse getBalances1Response() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "balances1.json").toFile(), BalanceResponse.class);
    }

    public static BalanceResponse getBalances2Response() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "balances2.json").toFile(), BalanceResponse.class);
    }

    public static TransactionsResponse getTransactionResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "transactions.json").toFile(),
                TransactionsResponse.class);
    }
}
