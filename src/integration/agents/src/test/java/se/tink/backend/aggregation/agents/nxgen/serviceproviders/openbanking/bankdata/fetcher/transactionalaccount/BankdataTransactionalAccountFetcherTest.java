package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;

public class BankdataTransactionalAccountFetcherTest {

    private static final Date DATE_BEFORE_LIMIT = DateUtils.addYears(new Date(), -6);
    private static final int DEFAULT_NO_OF_RETURNED_TRANSACTIONS = 2;
    private static final int NO_OF_TRANSACTIONS_WITH_ADDITIONAL_PAGE = 4;

    private BankdataApiClient apiClient = mock(BankdataApiClient.class);
    private BankdataTransactionalAccountFetcher bankdataTransactionalAccountFetcher;

    @Before
    public void setup() {
        bankdataTransactionalAccountFetcher = new BankdataTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldReturnTransactionResponse() {
        // Given
        when(apiClient.fetchTransactions(any(), any(), any()))
                .thenReturn(
                        BankDataTransactionalAccountFixtures.transactionResponseWithoutNextKey());

        // When
        PaginatorResponse result =
                bankdataTransactionalAccountFetcher.getTransactionsFor(
                        BankDataTransactionalAccountFixtures.transactionalAccount(),
                        Date.from(Instant.now()),
                        Date.from(Instant.now()));

        // Then
        verify(apiClient, times(0)).fetchNextTransactions(any());
        assertThat(result.getTinkTransactions().size())
                .isEqualTo(DEFAULT_NO_OF_RETURNED_TRANSACTIONS);
        assertThat(result.canFetchMore()).isEmpty();
    }

    @Test
    public void shouldReturnTransactionResponseWhenStartDateIsBeforeLimit() {
        // Given
        when(apiClient.fetchTransactions(any(), any(), any()))
                .thenReturn(
                        BankDataTransactionalAccountFixtures.transactionResponseWithoutNextKey());

        // When
        PaginatorResponse result =
                bankdataTransactionalAccountFetcher.getTransactionsFor(
                        BankDataTransactionalAccountFixtures.transactionalAccount(),
                        DATE_BEFORE_LIMIT,
                        Date.from(Instant.now()));

        // Then
        verify(apiClient, times(0)).fetchNextTransactions(any());
        assertThat(result.getTinkTransactions().size())
                .isEqualTo(DEFAULT_NO_OF_RETURNED_TRANSACTIONS);
        assertThat(result.canFetchMore()).isPresent();
    }

    @Test
    public void
            shouldReturnTransactionResponseWhenStartDateIsBeforeLimitAndThereAreNextTransactionsPages() {
        // Given
        when(apiClient.fetchTransactions(any(), any(), any()))
                .thenReturn(BankDataTransactionalAccountFixtures.transactionResponseWithNextKey());
        when(apiClient.fetchNextTransactions(any()))
                .thenReturn(
                        BankDataTransactionalAccountFixtures.transactionResponseWithoutNextKey());

        // When
        PaginatorResponse result =
                bankdataTransactionalAccountFetcher.getTransactionsFor(
                        BankDataTransactionalAccountFixtures.transactionalAccount(),
                        DATE_BEFORE_LIMIT,
                        Date.from(Instant.now()));

        // Then
        verify(apiClient).fetchNextTransactions(any());
        assertThat(result.getTinkTransactions().size())
                .isEqualTo(NO_OF_TRANSACTIONS_WITH_ADDITIONAL_PAGE);
        assertThat(result.canFetchMore()).isPresent();
    }

    @Test
    public void shouldReturnTransactionResponseWhenEndDateIsBeforeLimit() {
        // Given
        when(apiClient.fetchTransactions(any(), any(), any()))
                .thenReturn(
                        BankDataTransactionalAccountFixtures.transactionResponseWithoutNextKey());

        // When
        PaginatorResponse result =
                bankdataTransactionalAccountFetcher.getTransactionsFor(
                        BankDataTransactionalAccountFixtures.transactionalAccount(),
                        Date.from(Instant.now()),
                        DATE_BEFORE_LIMIT);

        // Then
        verifyZeroInteractions(apiClient);
        assertThat(result.getTinkTransactions()).isEmpty();
        assertThat(result.canFetchMore()).isPresent();
    }
}
