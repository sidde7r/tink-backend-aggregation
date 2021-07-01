package se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.ConsorsbankStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.consorsbank.client.ConsorsbankFetcherApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.TransactionMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class ConsorsbankTransactionFetcherTest {

    private static final String TEST_CONSENT_ID = "test_consent_id";

    private static final String TEST_IBAN = "DE70500105178163962419";
    private static final String TEST_API_IDENTIFIER = "test_api_identifier";

    private static final String TEST_URL_BALANCES =
            "https://xs2a.consorsbank.de/v1/accounts/asdf1234/balances";
    private static final String TEST_URL_NEXT_PAGE =
            "https://xs2a.consorsbank.de/v1/accounts/accId1234/transactions?bookingStatus=both&dateFrom=1970-01-01&pageIndex=1";

    private ConsorsbankFetcherApiClient mockApiClient;
    private ConsorsbankStorage mockStorage;
    private TransactionMapper mockTransactionMapper;
    private TransactionPaginationHelper mockTransactionPaginationHelper;

    private ConsorsbankTransactionFetcher transactionFetcher;

    private FetchTransactionsResponse pageWithNext =
            TestDataReader.readFromFile(
                    TestDataReader.TRANSACTIONS_WITH_NEXT, FetchTransactionsResponse.class);

    private FetchTransactionsResponse pageWithoutNext =
            TestDataReader.readFromFile(
                    TestDataReader.TRANSACTIONS_WITHOUT_NEXT, FetchTransactionsResponse.class);

    @Before
    public void setup() {
        mockApiClient = mock(ConsorsbankFetcherApiClient.class);
        mockStorage = mock(ConsorsbankStorage.class);
        mockTransactionMapper = mock(ConsorsbankTransactionMapper.class);
        mockTransactionPaginationHelper = mock(TransactionPaginationHelper.class);

        when(mockStorage.getConsentId()).thenReturn(TEST_CONSENT_ID);
        when(mockTransactionMapper.toTinkTransaction(any(), anyBoolean()))
                .thenReturn(Optional.empty());

        transactionFetcher =
                new ConsorsbankTransactionFetcher(
                        mockApiClient,
                        mockStorage,
                        mockTransactionMapper,
                        mockTransactionPaginationHelper);
    }

    @Test
    public void shouldReturnEmptyListWhenNoConsentGivenForTransactionsOfAccount() {
        // given
        TransactionalAccount testAccount = testAccount(true);
        when(mockStorage.getConsentAccess()).thenReturn(AccessEntity.builder().build());

        // when
        List<AggregationTransaction> aggregationTransactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(aggregationTransactions).isEmpty();
    }

    @Test
    public void shouldReturnEmptyListWhenAccountDoesNotHaveIban() {
        // given
        TransactionalAccount testAccount = testAccount(false);

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).isEmpty();
    }

    @Test
    public void shouldFetchNextPagesAsLongAsThereIsANextPage() {
        // given
        TransactionalAccount testAccount = testAccount(true);
        mockTransactionAccessToInclude(TEST_IBAN);

        when(mockApiClient.fetchTransactions(
                        eq(TEST_CONSENT_ID), eq(TEST_API_IDENTIFIER), any(LocalDate.class)))
                .thenReturn(pageWithNext);
        when(mockApiClient.fetchTransactions(TEST_CONSENT_ID, TEST_URL_NEXT_PAGE))
                .thenReturn(pageWithNext, pageWithNext, pageWithoutNext);

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).isEmpty();
        verify(mockApiClient)
                .fetchTransactions(
                        eq(TEST_CONSENT_ID), eq(TEST_API_IDENTIFIER), any(LocalDate.class));
        verify(mockApiClient, times(3)).fetchTransactions(TEST_CONSENT_ID, TEST_URL_NEXT_PAGE);
        verify(mockTransactionMapper, times(4)).toTinkTransactions(any());
    }

    @Test
    public void shouldFetchAllHistoryWhenNoDateSavedForAccount() {
        // given
        LocalDate expectedDate = LocalDate.of(1970, 1, 1);
        TransactionalAccount testAccount = testAccount(true);
        mockTransactionAccessToInclude(TEST_IBAN);
        when(mockApiClient.fetchTransactions(TEST_CONSENT_ID, TEST_API_IDENTIFIER, expectedDate))
                .thenReturn(pageWithoutNext);

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).isEmpty();
        verify(mockApiClient).fetchTransactions(TEST_CONSENT_ID, TEST_API_IDENTIFIER, expectedDate);
        verify(mockTransactionMapper).toTinkTransactions(any());
    }

    @Test
    public void shouldFromSavedDateWhenDateSavedForAccount() {
        // given
        LocalDate expectedDate = LocalDate.of(2019, 1, 1);
        TransactionalAccount testAccount = testAccount(true);
        mockTransactionAccessToInclude(TEST_IBAN);

        when(mockTransactionPaginationHelper.getTransactionDateLimit(testAccount))
                .thenReturn(
                        Optional.of(
                                Date.from(
                                        expectedDate
                                                .atStartOfDay()
                                                .atZone(ZoneId.systemDefault())
                                                .toInstant())));

        when(mockApiClient.fetchTransactions(TEST_CONSENT_ID, TEST_API_IDENTIFIER, expectedDate))
                .thenReturn(pageWithoutNext);

        // when
        List<AggregationTransaction> transactions =
                transactionFetcher.fetchTransactionsFor(testAccount);

        // then
        assertThat(transactions).isEmpty();
        verify(mockApiClient).fetchTransactions(TEST_CONSENT_ID, TEST_API_IDENTIFIER, expectedDate);
        verify(mockTransactionMapper).toTinkTransactions(any());
    }

    private TransactionalAccount testAccount(boolean withIban) {
        TransactionalAccount mock = mock(TransactionalAccount.class);
        List<AccountIdentifier> identifiers = new ArrayList<>();
        if (withIban) {
            identifiers.add(new IbanIdentifier("DE70500105178163962419"));
        }
        when(mock.getIdentifiers()).thenReturn(identifiers);
        when(mock.getApiIdentifier()).thenReturn(TEST_API_IDENTIFIER);
        return mock;
    }

    private void mockTransactionAccessToInclude(String iban) {
        List<AccountReferenceEntity> list = new ArrayList<>();
        if (iban != null) {
            list.add(new AccountReferenceEntity(iban));
        }
        when(mockStorage.getConsentAccess())
                .thenReturn(AccessEntity.builder().transactions(list).build());
    }
}
