package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.agents.nxgen.nl.banks.ics.TestHelper.getCreditCardAccount;
import static se.tink.backend.aggregation.agents.nxgen.nl.banks.ics.TestHelper.getCreditTransactionResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSTimeProvider;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSCreditCardFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(MockitoJUnitRunner.class)
public class ICSCreditCardFetcherTest {

    private final LocalDateTimeSource localDateTimeSource = new ConstantLocalDateTimeSource();

    private final ICSTimeProvider timeProvider =
            new ICSTimeProvider(localDateTimeSource, new PersistentStorage());

    private final LocalDate today = timeProvider.now();

    private final LocalDate earliestFromDate = localDate(timeProvider.getConsentTransactionDate());

    private final LocalDate fromDate = (today.minusDays(20));

    private final LocalDate toDate = (today.minusDays(10));

    private final CreditCardAccount account = getCreditCardAccount();

    private final String accountId = account.getApiIdentifier();

    @Mock private ICSApiClient client;

    @InjectMocks private ICSCreditCardFetcher creditCardFetcher;

    @Before
    public void setUp() {
        creditCardFetcher =
                new ICSCreditCardFetcher(
                        client, new ICSTimeProvider(localDateTimeSource, new PersistentStorage()));
    }

    @Test
    public void shouldFetchTransactions() {
        // given
        given(client.getTransactionsByDate(accountId, fromDate, toDate))
                .willReturn(getCreditTransactionResponse());

        // when
        PaginatorResponse response =
                creditCardFetcher.getTransactionsFor(account, date(fromDate), date(toDate));

        // then
        assertThat(response).usingRecursiveComparison().isEqualTo(getCreditTransactionResponse());
    }

    @Test
    public void shouldAdjustFromDateWhileFetchingTransactions() {
        // given
        LocalDate longLongTimeAgo = earliestFromDate.minusDays(1);

        // when
        creditCardFetcher.getTransactionsFor(account, date(longLongTimeAgo), date(toDate));

        // then
        then(client).should().getTransactionsByDate(accountId, earliestFromDate, toDate);
    }

    @Test
    public void shouldReturnEmptyResponseWhenToDateAndFromDateAreEqual() {
        // when
        PaginatorResponse response =
                creditCardFetcher.getTransactionsFor(account, date(toDate), date(toDate));

        // then
        assertThat(response).isEqualTo(PaginatorResponseImpl.createEmpty());
    }

    @Test
    public void shouldReturnEmptyResponseWhenToDateIsBeforeFromDate() {
        // given
        LocalDate earlierThanFromDate = fromDate.minusDays(1);

        // when
        PaginatorResponse response =
                creditCardFetcher.getTransactionsFor(
                        account, date(fromDate), date(earlierThanFromDate));

        // then
        assertThat(response).isEqualTo(PaginatorResponseImpl.createEmpty());
    }

    @Test
    public void shouldReturnEmptyResponseOnUnauthorizedStatusCode() {
        // given
        bankRespondsWithUnauthorizedStatusCode();

        // when
        PaginatorResponse response =
                creditCardFetcher.getTransactionsFor(account, date(fromDate), date(toDate));

        // then
        assertThat(response).isEqualTo(PaginatorResponseImpl.createEmpty(false));
    }

    private void bankRespondsWithUnauthorizedStatusCode() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);

        // and
        given(httpResponse.getStatus()).willReturn(SC_UNAUTHORIZED);

        // and
        given(client.getTransactionsByDate(any(), any(), any()))
                .willThrow(new HttpResponseException(mock(HttpRequest.class), httpResponse));
    }

    private Date date(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate localDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
