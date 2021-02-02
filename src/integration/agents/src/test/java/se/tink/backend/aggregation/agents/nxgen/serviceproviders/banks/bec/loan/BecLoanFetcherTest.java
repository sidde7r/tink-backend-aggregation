package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Locale;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecUrlConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.rpc.FetchLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.BecErrorResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.i18n.Catalog;

@RunWith(JUnitParamsRunner.class)
public class BecLoanFetcherTest {

    @Test
    @Parameters({
        "You have not taken out a mortgage loan through us\\, thus we cannot display any details. If you wish to gain access to mortgage loans here\\, we are most obliged to work on a solution for you. Please contact your financial advisor directly via the mobile bank.",
        "Du har ikke optaget et realkreditlån gennem os\\, og derfor kan vi ikke vise dig nogen oplysninger. Ønsker du at kunne se dit realkreditlån her\\, så står vi naturligvis klar til at regne på en god løsning for dig. Kontakt din rådgiver direkte her fra mobilbanken.",
        "Ingen oplysninger fundet.",
        "The required function is not currently available. Try again later.",
        "Den ønskede funktion er ikke tilgængelig i øjeblikket. Prøv igen senere."
    })
    public void fetch_accounts_returns_known_error(String message) {
        // given
        BecApiClient becApiClient = createBecApiClient(message);
        BecLoanFetcher becLoanFetcher = new BecLoanFetcher(becApiClient);
        // when
        Collection<LoanAccount> collection = becLoanFetcher.fetchAccounts();

        // then
        assertThat(collection).isEmpty();
    }

    @Test
    @Parameters({"other"})
    public void fetch_accounts_returns_unknown_error(String message) {
        // given
        BecApiClient becApiClient = createBecApiClient(message);
        BecLoanFetcher becLoanFetcher = new BecLoanFetcher(becApiClient);
        // when
        Throwable throwable = catchThrowable(becLoanFetcher::fetchAccounts);

        // then
        assertThat(throwable).isInstanceOf(HttpResponseException.class);
    }

    private BecApiClient createBecApiClient(String message) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        TinkHttpClient tinkHttpClient = mockHttpClient(requestBuilder);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(400);
        BecErrorResponse becErrorResponse = new BecErrorResponse();
        becErrorResponse.setMessage(message);
        when(httpResponse.getBody(BecErrorResponse.class)).thenReturn(becErrorResponse);
        when(requestBuilder.get(FetchLoanResponse.class))
                .thenThrow(new HttpResponseException("", mock(HttpRequest.class), httpResponse));
        return new BecApiClient(
                mock(BecSecurityHelper.class),
                tinkHttpClient,
                new BecUrlConfiguration(""),
                new Catalog(Locale.getDefault()));
    }

    public static TinkHttpClient mockHttpClient(RequestBuilder requestBuilder) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);

        when(tinkHttpClient.request(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        return tinkHttpClient;
    }
}
