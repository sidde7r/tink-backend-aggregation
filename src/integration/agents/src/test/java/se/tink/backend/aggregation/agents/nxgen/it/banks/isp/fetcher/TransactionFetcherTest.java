package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionFetcherTest {

    private TransactionFetcher fetcher;
    private IspApiClient apiClient;

    @Before
    public void setup() {
        this.apiClient = mock(IspApiClient.class);
        this.fetcher = new TransactionFetcher(apiClient);
    }

    @Test
    public void shouldFetchTransactions() {
        // given
        when(apiClient.fetchTransactions(eq("123456789"), any(), eq(0)))
                .thenReturn(FetchersTestData.fetchTransactionsResponse());
        when(apiClient.fetchTransactions(eq("123456789"), any(), eq(1)))
                .thenReturn(FetchersTestData.emptyTransactionsResponse());
        Account account = mock(Account.class);
        when(account.getApiIdentifier()).thenReturn("123456789");
        // when
        PaginatorResponse page0 = fetcher.getTransactionsFor(account, 0);
        // then
        assertThat(page0.canFetchMore()).isPresent();
        assertThat(page0.canFetchMore().get()).isTrue();
        List<Transaction> transactions = new ArrayList<>(page0.getTinkTransactions());
        assertThat(transactions).hasSize(2);
        Transaction transaction1 =
                transactions.stream()
                        .filter(transaction -> transaction.isPending())
                        .findAny()
                        .orElseThrow(IllegalArgumentException::new);
        assertThat(transaction1.isPending()).isTrue();
        assertThat(transaction1.getDescription()).isEqualTo("Bonifico disposto da: NAME TEST");
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2019-09-25");
        assertThat(transaction1.getExactAmount().getExactValue()).isEqualByComparingTo("709.80");
        // and when
        PaginatorResponse page1 = fetcher.getTransactionsFor(account, 1);
        // then
        assertThat(page1.canFetchMore()).isPresent();
        assertThat(page1.canFetchMore().get()).isFalse();
        assertThat(page1.getTinkTransactions()).hasSize(0);
    }
}
