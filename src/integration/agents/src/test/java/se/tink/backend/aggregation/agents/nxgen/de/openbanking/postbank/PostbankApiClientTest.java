package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PostbankApiClientTest {

    private static final String CONSENT_BAD_REQUEST_JSON =
            "{\"tppMessages\": [{\"category\": \"ERROR\", \"code\": \"Bad Request\", \"text\": \"The system has encountered a Technical/Server Error. Hence cannot process the request at this time. Please try again after sometime.\"} ], \"transactionStatus\": \"RJCT\"}";
    private static final String START_AUTHORISATION_INVALID_CREDENTIALS_JSON =
            "{\"tppMessages\": [{\"category\": \"ERROR\", \"code\": \"PSU_CREDENTIALS_INVALID\", \"text\": \"The credentials/authentication information entered are invalid. Please retry the request with correct authentication information\"} ] }";
    private static final String DIFFERENT_JSON = "{}";

    private static final String TEST_URL = "https://URL_1_2_3.example.com";
    private TinkHttpClient tinkClient;
    private PostbankApiClient apiClient;

    @Before
    public void before() {
        tinkClient = mock(TinkHttpClient.class);
        apiClient =
                new PostbankApiClient(
                        tinkClient,
                        null,
                        "",
                        new DeutscheMarketConfiguration(TEST_URL, "PSU_ID_TYPE"));
    }

    @Test
    public void shouldHandleBadRequestInGetConsentCorrectly() {
        // given
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        given(tinkClient.request(new URL(TEST_URL + "/v1/consents")))
                .willThrow(new HttpResponseException(null, mockHttpResponse));
        given(mockHttpResponse.getBody(ErrorResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                CONSENT_BAD_REQUEST_JSON, ErrorResponse.class));
        // when
        Throwable throwable = catchThrowable(() -> apiClient.getConsents("asdf", "asdf"));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldThrowHttpResponseExceptionInGetConsentInOtherCases() {
        // given
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        HttpResponseException httpResponseException =
                new HttpResponseException(null, mockHttpResponse);
        given(tinkClient.request(new URL(TEST_URL + "/v1/consents")))
                .willThrow(httpResponseException);
        given(mockHttpResponse.getBody(ErrorResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                DIFFERENT_JSON, ErrorResponse.class));
        // when
        Throwable throwable = catchThrowable(() -> apiClient.getConsents("asdf", "asdf"));

        // then
        assertThat(throwable).isEqualTo(httpResponseException);
    }

    @Test
    public void shouldHandleIncorrectCredentialsInStartAuthorisationCorrectly() {
        // given
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        given(tinkClient.request(new URL(TEST_URL)))
                .willThrow(new HttpResponseException(null, mockHttpResponse));
        given(mockHttpResponse.getBody(ErrorResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                START_AUTHORISATION_INVALID_CREDENTIALS_JSON, ErrorResponse.class));
        // when
        Throwable throwable =
                catchThrowable(
                        () -> apiClient.startAuthorisation(new URL(TEST_URL), "psuid", "password"));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldThrowHttpResponseExceptionInStartAuthorisationInOtherCases() {
        // given
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        HttpResponseException httpResponseException =
                new HttpResponseException(null, mockHttpResponse);
        given(tinkClient.request(new URL(TEST_URL))).willThrow(httpResponseException);
        given(mockHttpResponse.getBody(ErrorResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                DIFFERENT_JSON, ErrorResponse.class));
        // when
        Throwable throwable =
                catchThrowable(
                        () -> apiClient.startAuthorisation(new URL(TEST_URL), "psuid", "password"));

        // then
        assertThat(throwable).isEqualTo(httpResponseException);
    }
}
