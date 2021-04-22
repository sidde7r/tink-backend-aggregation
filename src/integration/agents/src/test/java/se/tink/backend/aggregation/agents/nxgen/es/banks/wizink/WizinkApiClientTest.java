package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc.ConsultTransactionResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WizinkApiClientTest {
    private static final String DUMMY_X_TOKEN_ID = "DUMMY_X_TOKEN_ID";
    private WizinkApiClient wizinkApiClient;
    private TinkHttpClient httpClient;
    private SupplementalInformationHelper supplementalInformationHelper;
    private WizinkStorage wizinkStorage;

    @Before
    public void setup() {
        httpClient = mock(TinkHttpClient.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        wizinkStorage = mock(WizinkStorage.class);
        wizinkApiClient =
                new WizinkApiClient(httpClient, wizinkStorage, supplementalInformationHelper);
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

    @Test(expected = SupplementalInfoException.class)
    public void shouldThrowSupplementalInfoExceptionWhenInvalidOtp() {
        // given
        prepareTestData("808");
        // when
        wizinkApiClient.fetchTransactionsOlderThan90Days("DUMMY", "DUMMY");
    }

    @Test(expected = SupplementalInfoException.class)
    public void shouldThrowSupplementalInfoExceptionWhenOtpExpired() {
        // given
        prepareTestData("807");
        // when
        wizinkApiClient.fetchTransactionsOlderThan90Days("DUMMY", "DUMMY");
    }

    private void prepareTestData(String errorCode) {
        when(supplementalInformationHelper.waitForOtpInput()).thenReturn("DUMMY_OTP");
        when(wizinkStorage.getXTokenId()).thenReturn(DUMMY_X_TOKEN_ID);

        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);
        when(requestBuilderMock.header(HeaderKeys.X_TOKEN_ID, DUMMY_X_TOKEN_ID))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.type(any(MediaType.class))).thenReturn(requestBuilderMock);
        when(httpClient.request(Urls.TRANSACTIONS)).thenReturn(requestBuilderMock);
        when(requestBuilderMock.body(any())).thenReturn(requestBuilderMock);
        when(requestBuilderMock.post(ConsultTransactionResponse.class))
                .thenReturn(prepareErrorResultResponse(errorCode));
    }

    private ConsultTransactionResponse prepareErrorResultResponse(String errorCode) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"ConsultTransactionResponse\": {\n"
                        + "        \"result\": {\n"
                        + "            \"code\": \""
                        + errorCode
                        + "\",\n"
                        + "            \"message\": \"OTP Expired\"\n"
                        + "        }\n"
                        + "    }\n"
                        + "}",
                ConsultTransactionResponse.class);
    }
}
