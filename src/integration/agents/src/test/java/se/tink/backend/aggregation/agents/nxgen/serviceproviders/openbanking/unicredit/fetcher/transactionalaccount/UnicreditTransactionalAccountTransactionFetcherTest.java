package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.file.Paths;
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

    private UnicreditBaseApiClient mockApiClient;
    private TransactionPaginationHelper mockPaginationHelper;

    private TransactionalAccount testAccount;

    private UnicreditTransactionalAccountTransactionFetcher transactionFetcher;

    @Before
    public void setup() {
        mockApiClient = mock(UnicreditBaseApiClient.class);
        mockPaginationHelper = mock(TransactionPaginationHelper.class);

        testAccount = mock(TransactionalAccount.class);

        transactionFetcher =
                new UnicreditTransactionalAccountTransactionFetcher(
                        mockApiClient, mockPaginationHelper);
    }

    @Test
    public void shouldFetchPagesTillNoMoreAvailable() {
        // given
        given(mockPaginationHelper.getContentWithRefreshDate(testAccount))
                .willReturn(Optional.empty());
        given(mockApiClient.getTransactionsFor(eq(testAccount), any(Date.class)))
                .willReturn(getTransactionsResponse(true));
        given(mockApiClient.getTransactionsForNextUrl(NEXT_URL))
                .willReturn(getTransactionsResponse(false));

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).hasSize(4);
        verify(mockApiClient).getTransactionsFor(eq(testAccount), any(Date.class));
        verify(mockApiClient).getTransactionsForNextUrl(NEXT_URL);
        verify(mockPaginationHelper).getContentWithRefreshDate(testAccount);

        verifyNoMoreInteractions(mockApiClient, mockPaginationHelper);
    }

    @Test
    public void shouldStartWithExpectedStartOfTimesDate() {
        // given
        Date expectedDate = new Date(0);
        given(mockPaginationHelper.getContentWithRefreshDate(testAccount))
                .willReturn(Optional.empty());
        given(mockApiClient.getTransactionsFor(testAccount, expectedDate))
                .willReturn(getTransactionsResponse(false));

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).hasSize(2);
        verify(mockApiClient).getTransactionsFor(testAccount, expectedDate);
        verify(mockPaginationHelper).getContentWithRefreshDate(testAccount);

        verifyNoMoreInteractions(mockApiClient, mockPaginationHelper);
    }

    @Test
    public void shouldStartWithExpectedDateBasedOnAccount() {
        // given
        Date expectedDate = new Date(150123);
        given(mockPaginationHelper.getContentWithRefreshDate(testAccount))
                .willReturn(Optional.of(expectedDate));
        given(mockApiClient.getTransactionsFor(testAccount, expectedDate))
                .willReturn(getTransactionsResponse(false));

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).hasSize(2);
        verify(mockApiClient).getTransactionsFor(testAccount, expectedDate);
        verify(mockPaginationHelper).getContentWithRefreshDate(testAccount);

        verifyNoMoreInteractions(mockApiClient, mockPaginationHelper);
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
