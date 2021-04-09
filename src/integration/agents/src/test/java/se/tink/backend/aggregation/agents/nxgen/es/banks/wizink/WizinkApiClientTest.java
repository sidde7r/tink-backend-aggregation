package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.rpc.FindMovementsResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class WizinkApiClientTest {

    private WizinkApiClient wizinkApiClient;
    private TinkHttpClient httpClient;

    @Before
    public void setup() {
        httpClient = mock(TinkHttpClient.class);
        wizinkApiClient =
                new WizinkApiClient(
                        httpClient,
                        mock(WizinkStorage.class),
                        mock(SupplementalInformationHelper.class));
    }

    @Test
    public void shouldThrowLoginExceptionWhenIncorrectCredentials() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(401);
        when(httpClient.request(Urls.LOGIN))
                .thenThrow(new HttpResponseException(null, httpResponse));

        // then
        Throwable thrown =
                catchThrowable(
                        () ->
                                wizinkApiClient.login(
                                        new CustomerLoginRequest("USERNAME", "PASSWORD")));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Ignore
    @Test
    public void shouldThrowLoginExceptionWhenWrongOtp() {
        // given
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getStatus()).thenReturn(409);
        FindMovementsResponse response = new FindMovementsResponse();
        when(response.getResult().getCode()).thenReturn(ErrorCodes.WRONG_OTP);
        when(httpClient.request(Urls.CARD_DETAIL_TRANSACTIONS))
                .thenThrow(new HttpResponseException(null, httpResponse));

        // then
        Throwable thrown =
                catchThrowable(
                        () ->
                                wizinkApiClient.fetchCreditCardTransactionsOlderThan90Days(
                                        any(), any()));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
    }
}
