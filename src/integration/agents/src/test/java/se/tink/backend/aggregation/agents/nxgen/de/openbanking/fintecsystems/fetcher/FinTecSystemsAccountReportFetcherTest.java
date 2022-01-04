package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fintecsystems.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.FinTecSystemsAccountReportFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.FinTecSystemsReportMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.data.FinTecSystemsReport;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class FinTecSystemsAccountReportFetcherTest {

    private static final String TEST_TRANSACTION_ID = "test_transaction_id";

    private FinTecSystemsApiClient mockApiClient;
    private FinTecSystemsStorage mockStorage;
    private FinTecSystemsReportMapper mockReportMapper;

    private FinTecSystemsAccountReportFetcher accountReportFetcher;

    @Before
    public void setup() {
        mockApiClient = mock(FinTecSystemsApiClient.class);
        mockStorage = mock(FinTecSystemsStorage.class);
        mockReportMapper = mock(FinTecSystemsReportMapper.class);

        accountReportFetcher =
                new FinTecSystemsAccountReportFetcher(mockApiClient, mockStorage, mockReportMapper);

        when(mockApiClient.fetchReport(TEST_TRANSACTION_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.REPORT, FinTecSystemsReport.class));
    }

    @Test
    public void shouldReturnEmptyListWhenNoPaymentExecutedSuccessfullyBefore() {
        // given
        when(mockStorage.retrieveTransactionId()).thenReturn(Optional.empty());

        // when
        Collection<TransactionalAccount> accounts = accountReportFetcher.fetchAccounts();

        // then
        assertThat(accounts).isEmpty();
    }

    @Test
    public void shouldThrowExceptionWhenMapperFailsToTransformReport() {
        // given
        when(mockStorage.retrieveTransactionId()).thenReturn(Optional.of(TEST_TRANSACTION_ID));
        when(mockReportMapper.transformReportToTinkAccount(any())).thenReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> accountReportFetcher.fetchAccounts());

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("FTS Agent failed to parse payment report! This should not happen.");
    }

    @Test
    public void shouldReturnTransformedReportWhenEverythingOk() {
        // given
        when(mockStorage.retrieveTransactionId()).thenReturn(Optional.of(TEST_TRANSACTION_ID));
        when(mockReportMapper.transformReportToTinkAccount(any()))
                .thenReturn(Optional.of(mock(TransactionalAccount.class)));

        // when
        Collection<TransactionalAccount> accounts = accountReportFetcher.fetchAccounts();

        // then
        assertThat(accounts).hasSize(1);
    }
}
