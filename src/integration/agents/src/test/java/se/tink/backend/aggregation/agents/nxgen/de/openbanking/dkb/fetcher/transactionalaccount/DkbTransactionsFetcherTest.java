package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@RunWith(JUnitParamsRunner.class)
public class DkbTransactionsFetcherTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
    private final Transaction mockTransaction = mock(Transaction.class);
    private final TransactionalAccount mockAccount = mock(TransactionalAccount.class);
    private final GetTransactionsResponse mockResponse = mock(GetTransactionsResponse.class);
    private final DkbApiClient mockApiClient = mock(DkbApiClient.class);
    private final DkbStorage mockStorage = mock(DkbStorage.class);
    private final LocalDateTimeSource mockLocalDateTimeSource = mock(LocalDateTimeSource.class);
    private DkbTransactionsFetcher transactionsFetcher;

    @Test
    @Parameters({"false, true", "true, false"})
    public void fetchTransactionsForShouldReturnTransactionsForLast90Days(
            boolean userAvailableForInteraction, boolean firstFetch) {
        // given
        List<Transaction> expectedResult = Arrays.asList(mockTransaction, mockTransaction);
        transactionsFetcher =
                new DkbTransactionsFetcher(
                        mockApiClient,
                        mockStorage,
                        userAvailableForInteraction,
                        mockLocalDateTimeSource);
        given(mockStorage.isFirstFetch()).willReturn(firstFetch);
        given(mockLocalDateTimeSource.now(ZONE_ID))
                .willReturn(LocalDateTime.parse("2022-01-14T00:55:00"));
        given(
                        mockApiClient.getTransactions(
                                mockAccount,
                                LocalDate.parse("2021-10-17"),
                                LocalDate.parse("2022-01-14")))
                .willReturn(mockResponse);
        given(mockResponse.toTinkTransactions()).willReturn(expectedResult);
        // when
        List<AggregationTransaction> result = transactionsFetcher.fetchTransactionsFor(mockAccount);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(mockStorage, never()).markFirstFetchAsDone();
    }

    @Test
    public void fetchTransactionsForShouldReturnAllTransactionsAndMarkFirstFetchAsDone() {
        // given
        List<Transaction> expectedResult = Arrays.asList(mockTransaction, mockTransaction);
        transactionsFetcher =
                new DkbTransactionsFetcher(
                        mockApiClient, mockStorage, true, mockLocalDateTimeSource);
        given(mockStorage.isFirstFetch()).willReturn(true);
        given(mockLocalDateTimeSource.now(ZONE_ID))
                .willReturn(LocalDateTime.parse("2022-01-14T00:01:00"));
        given(
                        mockApiClient.getTransactions(
                                mockAccount,
                                LocalDate.parse("1970-03-31"),
                                LocalDate.parse("2022-01-14")))
                .willReturn(mockResponse);
        given(mockResponse.toTinkTransactions()).willReturn(expectedResult);
        // when
        List<AggregationTransaction> result = transactionsFetcher.fetchTransactionsFor(mockAccount);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(mockStorage).markFirstFetchAsDone();
    }
}
