package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class MediolanumTransactionFetcherTest {

    private static final String TEST_ACC_ID = "test_account_id";
    private MediolanumApiClient mockApiClient;
    private TransactionMapper mockTransactionMapper;
    private TransactionalAccount mockAccount;

    private static LocalDate date90DaysAgo;
    private static LocalDate date5YearsAgo;
    private static LocalDate dateToday;

    @BeforeClass
    public static void setupClass() {
        ConstantLocalDateTimeSource constantLocalDateTimeSource = new ConstantLocalDateTimeSource();
        date90DaysAgo = constantLocalDateTimeSource.now().toLocalDate().minusDays(90);
        date5YearsAgo = constantLocalDateTimeSource.now().toLocalDate().minusYears(5);
        dateToday = constantLocalDateTimeSource.now().toLocalDate();
    }

    @Before
    public void setup() {
        mockApiClient = mock(MediolanumApiClient.class);
        mockTransactionMapper = mock(TransactionMapper.class);
        mockAccount = mock(TransactionalAccount.class);

        when(mockTransactionMapper.toTinkTransaction(any()))
                .thenReturn(Optional.of(mock(Transaction.class)));
        when(mockAccount.getApiIdentifier()).thenReturn(TEST_ACC_ID);
    }

    @Test
    public void shouldFetchOnly90DaysAndMapThemIfUserNotPresent() {
        // given
        MediolanumTransactionFetcher fetcher = buildFetcher(false);
        when(mockApiClient.fetchTransactions(TEST_ACC_ID, date90DaysAgo, dateToday))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.TWO_TRANSACTIONS, TransactionsResponse.class));

        // when
        List<AggregationTransaction> mappedTransactions = fetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(mappedTransactions).hasSize(2);
        verify(mockApiClient).fetchTransactions(TEST_ACC_ID, date90DaysAgo, dateToday);
        verify(mockTransactionMapper, times(2)).toTinkTransaction(any());
    }

    @Test
    public void shouldFetch5YearsAndMapThemIfUserPresent() {
        // given
        MediolanumTransactionFetcher fetcher = buildFetcher(true);
        when(mockApiClient.fetchTransactions(TEST_ACC_ID, date5YearsAgo, dateToday))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.TWO_TRANSACTIONS, TransactionsResponse.class));

        // when
        List<AggregationTransaction> mappedTransactions = fetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(mappedTransactions).hasSize(2);
        verify(mockApiClient).fetchTransactions(TEST_ACC_ID, date5YearsAgo, dateToday);
        verify(mockTransactionMapper, times(2)).toTinkTransaction(any());
    }

    @Test
    public void shouldHandleEmptyTransactionsCorrectly() {
        // given
        MediolanumTransactionFetcher fetcher = buildFetcher(true);
        when(mockApiClient.fetchTransactions(TEST_ACC_ID, date5YearsAgo, dateToday))
                .thenReturn(new TransactionsResponse());

        // when
        List<AggregationTransaction> mappedTransactions = fetcher.fetchTransactionsFor(mockAccount);

        // then
        assertThat(mappedTransactions).hasSize(0);
        verify(mockApiClient).fetchTransactions(TEST_ACC_ID, date5YearsAgo, dateToday);
        verifyNoMoreInteractions(mockTransactionMapper);
    }

    private MediolanumTransactionFetcher buildFetcher(boolean userPresent) {
        return new MediolanumTransactionFetcher(
                mockApiClient,
                mockTransactionMapper,
                new ConstantLocalDateTimeSource(),
                userPresent);
    }
}
