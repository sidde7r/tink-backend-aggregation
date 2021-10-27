package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class HandelsbankenBaseCreditCardFetcherTest {
    private static final LocalDate MAX_PERIOD_TRANSACTIONS = LocalDate.of(2020, 2, 1);
    private static final Date MAX_DATE = new Date(1580515200000L);
    private static final Date BEFORE_MAX_DATE = new Date(1577876818000L); // 2020-01-01
    private static final Date RECENT_DATE = new Date(1609499218000L); // 2021-01-01
    private static final String DUMMY_ACCOUNT_ID = "ee02d6d8-6225-467d-bc69-a0dc03894642";

    private HandelsbankenBaseApiClient apiClient;
    private HandelsbankenBaseCreditCardFetcher creditCardFetcher;
    CreditCardAccount creditCardAccount;

    @Before
    public void setup() {
        this.apiClient = mock(HandelsbankenBaseApiClient.class);
        this.creditCardFetcher =
                new HandelsbankenBaseCreditCardFetcher(apiClient, MAX_PERIOD_TRANSACTIONS);
        this.creditCardAccount = mock(CreditCardAccount.class);
        given(creditCardAccount.getApiIdentifier()).willReturn(DUMMY_ACCOUNT_ID);
        when(apiClient.getCreditTransactions(any(), any(), any()))
                .thenReturn(mock(TransactionResponse.class));
    }

    @Test
    public void shouldNotFetchLongerHistoryThanMaxDate() {
        // when
        creditCardFetcher.getTransactionsFor(creditCardAccount, BEFORE_MAX_DATE, RECENT_DATE);

        // then
        verify(apiClient).getCreditTransactions(DUMMY_ACCOUNT_ID, MAX_DATE, RECENT_DATE);
    }

    @Test
    public void shouldNotAttemptToFetchTransactionsIfFromDateIsLaterThanToDate() {
        // when
        PaginatorResponse response =
                creditCardFetcher.getTransactionsFor(creditCardAccount, RECENT_DATE, MAX_DATE);

        // then
        verify(apiClient, never()).getCreditTransactions(any(), any(), any());
        assertThat(response.getTinkTransactions()).isEmpty();
    }
}
