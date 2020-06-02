package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BnpParibasTransactionFetcherTest {

    private static final ZoneId ZONE_ID = ZoneId.of("UTC");
    private static final Instant NOW = Instant.now();
    private static final LocalDate TODAY = NOW.atZone(ZONE_ID).toLocalDate();
    private static final String ACCOUNT_ID = "DUMMY_ACCOUNT_ID";

    private BnpParibasTransactionFetcher bnpParibasTransactionFetcher;

    private BnpParibasApiBaseClient apiClientMock;

    @Before
    public void init() {
        final Clock clockMock = createClockMock();

        apiClientMock = mock(BnpParibasApiBaseClient.class);
        bnpParibasTransactionFetcher = new BnpParibasTransactionFetcher(apiClientMock, clockMock);
    }

    @Test
    public void shouldGetTransactionsWithin13MonthsLimit() {
        // given
        final TransactionalAccount account = createAccountMock();
        final Date todayMinus13Months = convertLocalDateToDate(TODAY.minusMonths(13L));
        final Date todayMinus10Months = convertLocalDateToDate(TODAY.minusMonths(10L));
        final TransactionsResponse expectedResponse = mock(TransactionsResponse.class);

        when(apiClientMock.getTransactions(ACCOUNT_ID, todayMinus13Months, todayMinus10Months))
                .thenReturn(expectedResponse);

        // when
        final PaginatorResponse returnedResponse =
                bnpParibasTransactionFetcher.getTransactionsFor(
                        account, todayMinus13Months, todayMinus10Months);

        // then
        assertThat(returnedResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetTransactionsWhenSomeAreBeyond13MonthsLimit() {
        // given
        final TransactionalAccount account = createAccountMock();
        final Date todayMinus14Months = convertLocalDateToDate(TODAY.minusMonths(14L));
        final Date todayMinus13Months = convertLocalDateToDate(TODAY.minusMonths(13L));
        final TransactionsResponse expectedResponse = mock(TransactionsResponse.class);

        when(apiClientMock.getTransactions(ACCOUNT_ID, todayMinus13Months, todayMinus13Months))
                .thenReturn(expectedResponse);

        // when
        final PaginatorResponse returnedResponse =
                bnpParibasTransactionFetcher.getTransactionsFor(
                        account, todayMinus14Months, todayMinus13Months);

        // then
        assertThat(returnedResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetNoTransactionsWhenAllAreBeyond13MonthsLimit() {
        // given
        final TransactionalAccount account = createAccountMock();
        final Date todayMinus15Months = convertLocalDateToDate(TODAY.minusMonths(15L));
        final Date todayMinus14Months = convertLocalDateToDate(TODAY.minusMonths(14L));

        // when
        final PaginatorResponse returnedResponse =
                bnpParibasTransactionFetcher.getTransactionsFor(
                        account, todayMinus15Months, todayMinus14Months);

        // then
        assertThat(returnedResponse.getTinkTransactions()).isEmpty();
        assertThat(returnedResponse.canFetchMore().isPresent()).isTrue();
        assertThat(returnedResponse.canFetchMore().get()).isFalse();

        verify(apiClientMock, never()).getTransactions(anyString(), any(), any());
    }

    private static TransactionalAccount createAccountMock() {
        final TransactionalAccount accountMock = mock(TransactionalAccount.class);

        when(accountMock.getAccountNumber()).thenReturn(ACCOUNT_ID);

        return accountMock;
    }

    private static Clock createClockMock() {
        final Clock clockMock = mock(Clock.class);

        when(clockMock.instant()).thenReturn(NOW);
        when(clockMock.getZone()).thenReturn(ZONE_ID);

        return clockMock;
    }

    private static Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZONE_ID).toInstant());
    }
}
