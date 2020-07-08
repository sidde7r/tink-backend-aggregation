package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BoursoramaTransactionalAccountTransactionFetcherTest {

    private static final String TRANSACTIONS_WITH_CONTINUATION_KEY_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/boursorama/resources/transactions_response_with_continuation_key.json";

    private static final String TRANSACTIONS_WITHOUT_CONTINUATION_KEY_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/boursorama/resources/transactions_response_without_continuation_key.json";
    private static final String TEST_ACCOUNT_KEY = "testAccountKey";

    private BoursoramaTransactionalAccountTransactionFetcher transactionFetcher;
    private BoursoramaApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(BoursoramaApiClient.class);
        transactionFetcher = new BoursoramaTransactionalAccountTransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactions() {
        // given
        TransactionalAccount account = givenAccount();
        givenApiClientWillReturnTwoPagesOfTransactions();
        // when
        PaginatorResponse page1 = transactionFetcher.fetchTransactionsFor(account);
        // then
        assertThatPage1ContainsContinuationKey(page1);
        assertThatPage1ContainsExpectedTransactions(page1);
        // and when
        PaginatorResponse page2 = transactionFetcher.fetchTransactionsFor(account);
        // then
        assertThatPage2DoesNotContainContinuationKey(page2);
        assertThatPage2ContainsExpectedTransactions(page2);
    }

    private void assertThatPage2ContainsExpectedTransactions(PaginatorResponse response) {
        Collection<? extends Transaction> transactions = response.getTinkTransactions();
        assertThat(transactions).hasSize(1);
        Transaction transaction1 =
                transactions.stream().findAny().orElseThrow(IllegalArgumentException::new);
        assertThat(transaction1.getExactAmount().getCurrencyCode()).isEqualTo("EUR");
        assertThat(transaction1.getExactAmount().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2017-09-05");
        assertThat(transaction1.getDescription()).isEqualTo("VIR VIREMENT CREATION COMPTE");
    }

    private void assertThatPage2DoesNotContainContinuationKey(PaginatorResponse page2) {
        assertThat(page2.canFetchMore()).isPresent();
        assertThat(page2.canFetchMore().get()).isFalse();
    }

    private void assertThatPage1ContainsExpectedTransactions(PaginatorResponse response) {
        Collection<? extends Transaction> transactions = response.getTinkTransactions();
        assertThat(transactions).hasSize(1);
        Transaction transaction1 =
                transactions.stream().findAny().orElseThrow(IllegalArgumentException::new);
        assertThat(transaction1.getExactAmount().getCurrencyCode()).isEqualTo("EUR");
        assertThat(transaction1.getExactAmount().getExactValue())
                .isEqualByComparingTo(BigDecimal.valueOf(-13.0));
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2020-07-10");
        assertThat(transaction1.getDescription()).isEqualTo("PRLV SEPA SFR FIXE ADSL");
    }

    private void assertThatPage1ContainsContinuationKey(PaginatorResponse page1) {
        assertThat(page1.canFetchMore()).isPresent();
        //noinspection OptionalGetWithoutIsPresent
        assertThat(page1.canFetchMore().get()).isTrue();
    }

    private void givenApiClientWillReturnTwoPagesOfTransactions() {
        when(apiClient.getTransactions(eq(TEST_ACCOUNT_KEY), eq("testContinuationKey")))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(TRANSACTIONS_WITHOUT_CONTINUATION_KEY_FILE_PATH),
                                TransactionsResponse.class));
        when(apiClient.getTransactions(eq(TEST_ACCOUNT_KEY), eq(null)))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(TRANSACTIONS_WITH_CONTINUATION_KEY_FILE_PATH),
                                TransactionsResponse.class));
    }

    private TransactionalAccount givenAccount() {
        TransactionalAccount account = mock(TransactionalAccount.class);
        when(account.getFromTemporaryStorage(eq(BoursoramaConstants.Storage.ACCOUNT_KEY)))
                .thenReturn(TEST_ACCOUNT_KEY);
        return account;
    }
}
