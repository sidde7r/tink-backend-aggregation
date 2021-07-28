package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaFetcherApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.TransactionMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SpardaTransactionFetcherTest {

    private static final String TEST_ACC_ID = "TEST_ACC_IDENTIFIER";

    @Test
    public void shouldTryFetching90DaysWhenAskedToFetchedAroundMidnightCET() {
        // given
        SpardaFetcherApiClient mockApiClient = mock(SpardaFetcherApiClient.class);
        TransactionMapper mockMapper = mock(TransactionMapper.class);
        TransactionPaginationHelper mockPaginationHelper = mock(TransactionPaginationHelper.class);
        LocalDateTimeSource mockDateTimeSource = mock(LocalDateTimeSource.class);
        TransactionalAccount mock = mock(TransactionalAccount.class);

        LocalDate expectedDate = LocalDate.of(2021, 7, 13);
        LocalDateTime testNow = LocalDateTime.of(2021, 10, 10, 22, 30, 30);
        when(mockDateTimeSource.getInstant())
                .thenReturn(testNow.atZone(ZoneId.of("UTC")).toInstant());

        when(mockPaginationHelper.getTransactionDateLimit(any())).thenReturn(Optional.empty());
        when(mockApiClient.fetchTransactions(TEST_ACC_ID, expectedDate))
                .thenReturn(new FetchTransactionsResponse());

        when(mock.getApiIdentifier()).thenReturn(TEST_ACC_ID);

        SpardaTransactionFetcher transactionFetcher =
                new SpardaTransactionFetcher(
                        mockApiClient, mockMapper, mockPaginationHelper, mockDateTimeSource);

        // when
        List<AggregationTransaction> aggregationTransactions =
                transactionFetcher.fetchTransactionsFor(mock);

        // then
        assertThat(aggregationTransactions).isEmpty();
        verify(mockApiClient).fetchTransactions(TEST_ACC_ID, expectedDate);
    }
}
