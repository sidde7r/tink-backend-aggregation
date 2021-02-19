package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.GoalDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.identifiers.TinkIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class LunarTransactionFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources";

    private static final String ORIGIN_GROUP_ID = "833293fc-282c-4b99-8b86-2035218abeac";
    private static final String GOAL_ID = "59aec9ca-51df-47b8-aaeb-2121b47b99b0";
    private static final String CURRENCY = "DKK";

    private LunarTransactionFetcher transactionFetcher;
    private FetcherApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(FetcherApiClient.class);
        transactionFetcher = new LunarTransactionFetcher(apiClient);
    }

    @Test
    @Parameters(method = "checkingAccountParams")
    public void shouldGetTransactionsForCheckingAccount(
            TransactionsResponse transactionsResponse,
            TransactionKeyPaginatorResponse<String> expected) {
        // given
        when(apiClient.fetchTransactions(ORIGIN_GROUP_ID)).thenReturn(transactionsResponse);

        // when
        TransactionKeyPaginatorResponse<String> result =
                transactionFetcher.getTransactionsFor(
                        getTestAccount(TransactionalAccountType.CHECKING, ORIGIN_GROUP_ID), null);

        // then
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    private Object[] checkingAccountParams() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactions_response.json").toFile(),
                        TransactionsResponse.class),
                getExpectedTransactionsForCheckingAccountResponse()
            },
            new Object[] {
                new TransactionsResponse(), TransactionKeyPaginatorResponseImpl.createEmpty()
            },
        };
    }

    @Test
    @Parameters(method = "savingsAccountParams")
    public void shouldGetTransactionsForSavingsAccount(
            GoalDetailsResponse goalDetailsResponse,
            TransactionKeyPaginatorResponse<String> expected) {
        // given
        when(apiClient.fetchGoalDetails(GOAL_ID)).thenReturn(goalDetailsResponse);

        // when
        TransactionKeyPaginatorResponse<String> result =
                transactionFetcher.getTransactionsFor(
                        getTestAccount(TransactionalAccountType.SAVINGS, GOAL_ID), null);

        // then
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expected);
    }

    private Object[] savingsAccountParams() {
        return new Object[] {
            new Object[] {
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "goal_details_response.json").toFile(),
                        GoalDetailsResponse.class),
                getExpectedTransactionsForSavingsAccountResponse()
            },
            new Object[] {
                new GoalDetailsResponse(), TransactionKeyPaginatorResponseImpl.createEmpty()
            },
        };
    }

    private TransactionalAccount getTestAccount(TransactionalAccountType accountType, String id) {
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withoutFlags()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.zero(CURRENCY)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("unique_identifier")
                                .withAccountNumber("account_number")
                                .withAccountName("name")
                                .addIdentifier(new TinkIdentifier(id))
                                .build())
                .setApiIdentifier(id)
                .build()
                .orElseThrow(IllegalStateException::new);
    }

    private TransactionKeyPaginatorResponse<String>
            getExpectedTransactionsForCheckingAccountResponse() {
        List<Transaction> expectedTransactions =
                Arrays.asList(
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(-100, CURRENCY))
                                .setDate(new Date(1592278200000L))
                                .setDescription("Deposit")
                                .build(),
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(1234.12, CURRENCY))
                                .setDate(new Date(1591343356482L))
                                .setDescription("Transfer")
                                .build());
        return new TransactionKeyPaginatorResponseImpl<>(expectedTransactions, null);
    }

    private TransactionKeyPaginatorResponse<String>
            getExpectedTransactionsForSavingsAccountResponse() {
        List<Transaction> expectedTransactions =
                Collections.singletonList(
                        Transaction.builder()
                                .setAmount(ExactCurrencyAmount.of(1.01, CURRENCY))
                                .setDate(new Date(1613551491501L))
                                .setDescription("You added funds")
                                .build());
        return new TransactionKeyPaginatorResponseImpl<>(expectedTransactions, null);
    }
}
