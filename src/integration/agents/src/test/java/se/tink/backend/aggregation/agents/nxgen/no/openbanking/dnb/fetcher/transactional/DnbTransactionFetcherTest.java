package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.google.common.collect.ImmutableList;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbStorage;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.TransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper.DnbTransactionMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbTransactionFetcherTest {

    private static final String FROM_DATE = "1970-01-01";

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final String TEST_ACCOUNT_ID = "test_account_id";
    private static final String TEST_NEXT_URL_PART =
            "/v1/accounts/2132123131/transactions?dateFrom=2019-10-03&dateTo=2020-11-03&pageId=2020102000188458752240000001&bookingStatus=both";

    private DnbStorage mockStorage;
    private DnbApiClient mockApiClient;
    private DnbTransactionMapper mockTransactionMapper;
    private UserAvailability mockUserAvailability;
    private LocalDateTimeSource mockLocalDateTimeSource;

    private DnbTransactionFetcher transactionFetcher;

    private TransactionalAccount testAccount;
    private Collection<Transaction> testMappedTransactions;

    @Before
    public void setup() {
        mockStorage = mock(DnbStorage.class);
        mockApiClient = mock(DnbApiClient.class);
        mockTransactionMapper = mock(DnbTransactionMapper.class);
        mockUserAvailability = mock(UserAvailability.class);
        mockLocalDateTimeSource = mock(LocalDateTimeSource.class);

        transactionFetcher =
                new DnbTransactionFetcher(
                        mockStorage,
                        mockApiClient,
                        mockTransactionMapper,
                        mockUserAvailability,
                        mockLocalDateTimeSource);

        testAccount = mock(TransactionalAccount.class);

        given(mockStorage.getConsentId()).willReturn(TEST_CONSENT_ID);

        Transaction dummyTransaction = mock(Transaction.class);
        testMappedTransactions = ImmutableList.of(dummyTransaction, dummyTransaction);
        given(mockTransactionMapper.toTinkTransactions(any(TransactionEntity.class)))
                .willReturn(testMappedTransactions);
    }

    @Test
    public void shouldReturnPageWithSomeTransactionsAndNextKey() {
        // given
        given(testAccount.getApiIdentifier()).willReturn(TEST_ACCOUNT_ID);
        given(mockApiClient.fetchTransactions(FROM_DATE, TEST_CONSENT_ID, TEST_ACCOUNT_ID))
                .willReturn(getTransactionsResponse());
        given(mockUserAvailability.isUserPresent()).willReturn(true);

        // when
        TransactionKeyPaginatorResponse<String> pageOfTransactions =
                transactionFetcher.getTransactionsFor(testAccount, null);

        // then
        assertThat(pageOfTransactions.nextKey()).isEqualTo(TEST_NEXT_URL_PART);
        assertThat(pageOfTransactions.getTinkTransactions()).hasSameSizeAs(testMappedTransactions);

        verify(testAccount).getApiIdentifier();
        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchTransactions(FROM_DATE, TEST_CONSENT_ID, TEST_ACCOUNT_ID);
        verify(mockTransactionMapper).toTinkTransactions(any(TransactionEntity.class));
        verifyNoMoreInteractionsOnAllMocks();
    }

    @Test
    public void shouldFetchSubsequentPageIfContinuationKeyPresent() {
        // given
        given(mockUserAvailability.isUserPresent()).willReturn(true);
        given(mockApiClient.fetchNextTransactions(TEST_CONSENT_ID, TEST_NEXT_URL_PART))
                .willReturn(getTransactionsLastPageResponse());

        // when
        TransactionKeyPaginatorResponse<String> pageOfTransactions =
                transactionFetcher.getTransactionsFor(testAccount, TEST_NEXT_URL_PART);

        // then
        assertThat(pageOfTransactions.nextKey()).isNull();
        assertThat(pageOfTransactions.getTinkTransactions()).hasSameSizeAs(testMappedTransactions);

        verify(mockStorage).getConsentId();
        verify(mockApiClient).fetchNextTransactions(TEST_CONSENT_ID, TEST_NEXT_URL_PART);
        verify(mockTransactionMapper).toTinkTransactions(any(TransactionEntity.class));
        verifyNoMoreInteractionsOnAllMocks();
    }

    @Test
    public void shouldFetchTransactionsFromLast89DaysWhenUserIsNotAvailable() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2021, Month.APRIL, 19, 12, 0);
        LocalDate fromDate = startDate.minusDays(89).toLocalDate();

        given(mockUserAvailability.isUserPresent()).willReturn(false);
        given(mockLocalDateTimeSource.now()).willReturn(startDate);
        given(testAccount.getApiIdentifier()).willReturn(TEST_ACCOUNT_ID);
        given(
                        mockApiClient.fetchTransactions(
                                fromDate.toString(), TEST_CONSENT_ID, TEST_ACCOUNT_ID))
                .willReturn(getTransactionsResponse());

        // when
        transactionFetcher.getTransactionsFor(testAccount, null);

        // then
        verify(testAccount).getApiIdentifier();
        verify(mockStorage).getConsentId();
        verify(mockApiClient)
                .fetchTransactions(fromDate.toString(), TEST_CONSENT_ID, TEST_ACCOUNT_ID);
        verify(mockTransactionMapper).toTinkTransactions(any(TransactionEntity.class));
        verifyNoMoreInteractionsOnAllMocks();
    }

    private void verifyNoMoreInteractionsOnAllMocks() {
        verifyNoMoreInteractions(mockStorage, mockApiClient, mockTransactionMapper, testAccount);
    }

    private TransactionResponse getTransactionsResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "transactions.json").toFile(), TransactionResponse.class);
    }

    private TransactionResponse getTransactionsLastPageResponse() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "transactionsLastPage.json").toFile(),
                TransactionResponse.class);
    }
}
