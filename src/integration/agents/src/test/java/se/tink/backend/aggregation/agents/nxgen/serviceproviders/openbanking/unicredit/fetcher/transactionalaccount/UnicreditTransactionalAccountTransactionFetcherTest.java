package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class UnicreditTransactionalAccountTransactionFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/unicredit/resources";
    private static final URL NEXT_URL =
            new URL(
                    "http://example.com/hydrogen/v1/accounts/1234/transactions?dateFrom=1970-01-01&dateTo=2020-11-27&entryReferenceFrom=1234111&bookingStatus=booked&sessionId=zxcv-asdf");

    private UnicreditBaseApiClient apiClient;
    private TransactionPaginationHelper paginationHelper;
    private UnicreditTransactionsDateFromChooser unicreditTransactionsDateFromChooser;
    private TransactionalAccount testAccount;
    private UnicreditTransactionalAccountTransactionFetcher transactionFetcher;

    @Before
    public void setup() {
        apiClient = mock(UnicreditBaseApiClient.class);
        paginationHelper = mock(TransactionPaginationHelper.class);
        unicreditTransactionsDateFromChooser = mock(UnicreditTransactionsDateFromChooser.class);

        testAccount = mock(TransactionalAccount.class);

        transactionFetcher =
                new UnicreditTransactionalAccountTransactionFetcher(
                        apiClient, paginationHelper, unicreditTransactionsDateFromChooser);
    }

    @Test
    public void shouldFetchPagesTillNoMoreAvailable() {
        // given
        given(paginationHelper.getTransactionDateLimit(testAccount)).willReturn(Optional.empty());
        given(apiClient.getTransactionsFor(eq(testAccount), any(LocalDate.class)))
                .willReturn(getTransactionsResponse(true));
        given(apiClient.getTransactionsForNextUrl(NEXT_URL))
                .willReturn(getTransactionsResponse(false));
        given(unicreditTransactionsDateFromChooser.getDateFrom(Optional.empty()))
                .willReturn(LocalDate.ofEpochDay(1));

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).hasSize(4);
        verify(apiClient).getTransactionsFor(eq(testAccount), any(LocalDate.class));
        verify(apiClient).getTransactionsForNextUrl(NEXT_URL);
        verify(paginationHelper).getTransactionDateLimit(testAccount);

        verifyNoMoreInteractions(apiClient, paginationHelper);
    }

    @Test
    public void shouldStartWithExpectedStartOfTimesDate() {
        // given
        LocalDate expectedDate = LocalDate.ofEpochDay(1);
        given(paginationHelper.getTransactionDateLimit(testAccount)).willReturn(Optional.empty());
        given(apiClient.getTransactionsFor(testAccount, expectedDate))
                .willReturn(getTransactionsResponse(false));
        given(unicreditTransactionsDateFromChooser.getDateFrom(Optional.empty()))
                .willReturn(LocalDate.ofEpochDay(1));

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).hasSize(2);
        verify(apiClient).getTransactionsFor(testAccount, expectedDate);
        verify(paginationHelper).getTransactionDateLimit(testAccount);

        verifyNoMoreInteractions(apiClient, paginationHelper);
    }

    @Test
    public void shouldStartWithExpectedDateBasedOnAccount() {
        // given
        Date expectedDate = new Date(150123);
        LocalDate expectedLocalDate =
                expectedDate.toInstant().atOffset(ZoneOffset.UTC).toLocalDate();
        given(paginationHelper.getTransactionDateLimit(testAccount))
                .willReturn(Optional.of(expectedDate));
        given(apiClient.getTransactionsFor(testAccount, expectedLocalDate))
                .willReturn(getTransactionsResponse(false));
        given(unicreditTransactionsDateFromChooser.getDateFrom(Optional.of(expectedLocalDate)))
                .willReturn(expectedLocalDate);

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).hasSize(2);
        verify(apiClient).getTransactionsFor(testAccount, expectedLocalDate);
        verify(paginationHelper).getTransactionDateLimit(testAccount);

        verifyNoMoreInteractions(apiClient, paginationHelper);
    }

    private TransactionsResponse getTransactionsResponse(boolean hasNextPage) {
        return SerializationUtils.deserializeFromString(
                Paths.get(
                                TEST_DATA_PATH,
                                hasNextPage ? "transactions.json" : "transactionsLastPage.json")
                        .toFile(),
                TransactionsResponse.class);
    }
}
