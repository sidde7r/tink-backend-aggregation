package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.nl.banks.ics.TestHelper.TEST_MESSAGE;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ICSCreditCardFetcherTest {

    private ICSApiClient client;
    private ICSCreditCardFetcher icsCreditCardFetcher;

    private final Date fromDate = new Date();
    private final Date toDate = new Date();

    @Before
    public void setUp() throws Exception {
        fromDate.setTime(99999999);
        toDate.setTime(9999999999999L);
        client = mock(ICSApiClient.class);
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        icsCreditCardFetcher = new ICSCreditCardFetcher(client, persistentStorage);
    }

    @Test
    public void shouldReturnEmptyPaginatorResponseWhenToDateIsBiggerOrEqualFromDate() {
        // given
        HttpResponseException responseException = Mockito.mock(HttpResponseException.class);
        PaginatorResponse paginatorResponse = PaginatorResponseImpl.createEmpty();

        // when
        when(client.getTransactionsByDate(
                        TestHelper.ACCOUNT_ID, LocalDate.now().minusDays(2), LocalDate.now()))
                .thenThrow(responseException);

        // then
        assertThat(
                        icsCreditCardFetcher.getTransactionsFor(
                                TestHelper.getCreditCardAccount(), fromDate, fromDate))
                .usingRecursiveComparison()
                .isEqualTo(paginatorResponse);
    }

    @Test
    public void shouldReturnEmptyPaginatorResponseWhenToDateIsEarlierFromFromDate() {
        // given
        PaginatorResponse paginatorResponse = PaginatorResponseImpl.createEmpty(false);
        LocalDate localDate = LocalDate.of(2286, 11, 20);

        // when
        doThrow(mockException(HttpStatus.SC_UNAUTHORIZED))
                .when(client)
                .getTransactionsByDate(TestHelper.ACCOUNT_ID, getFallbackFromDate(), localDate);

        // then
        assertThat(
                        icsCreditCardFetcher.getTransactionsFor(
                                TestHelper.getCreditCardAccount(), fromDate, toDate))
                .usingRecursiveComparison()
                .isEqualTo(paginatorResponse);
    }

    @Test
    public void shouldReturnWhenToDateIsEarlierFromFromDate() {
        // given
        LocalDate localDate = LocalDate.of(2286, 11, 20);

        // when
        doThrow(mockException(HttpStatus.SC_OK))
                .when(client)
                .getTransactionsByDate(TestHelper.ACCOUNT_ID, getFallbackFromDate(), localDate);

        // then
        assertThatThrownBy(
                        () ->
                                icsCreditCardFetcher.getTransactionsFor(
                                        TestHelper.getCreditCardAccount(), fromDate, toDate))
                .isInstanceOf(HttpResponseException.class)
                .hasMessage(TEST_MESSAGE);
    }

    @Test
    public void shouldReturnCreditTransactionResponseWhenResponseIsAvailable() {
        // given
        CreditTransactionsResponse creditTransactionsResponse =
                TestHelper.getCreditTransactionResponse();
        LocalDate localDate = LocalDate.of(2286, 11, 20);

        // when
        doReturn(creditTransactionsResponse)
                .when(client)
                .getTransactionsByDate(TestHelper.ACCOUNT_ID, getFallbackFromDate(), localDate);

        // then
        assertThat(
                        icsCreditCardFetcher.getTransactionsFor(
                                TestHelper.getCreditCardAccount(), fromDate, toDate))
                .usingRecursiveComparison()
                .isEqualTo(creditTransactionsResponse);
    }

    private LocalDate getFallbackFromDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -2);
        c.add(Calendar.DATE, -89);
        return c.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private HttpResponseException mockException(int status) {
        HttpResponseException exception = mock(HttpResponseException.class, RETURNS_DEEP_STUBS);
        when((Object) exception.getResponse().getStatus()).thenReturn(status);
        when((Object) exception.getMessage()).thenReturn(TestHelper.TEST_MESSAGE);
        return exception;
    }
}
