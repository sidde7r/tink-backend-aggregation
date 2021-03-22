package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.transactions.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class PostbankApiClientTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/postbank/resources";

    private static final String CONSENT_AUTH_CREDENTIALS_INVALID =
            "consent_auth_credentials_invalid.json";
    private static final String CONSENT_CREATION_BAD_REQUEST = "consent_creation_bad_request.json";
    private static final String CONSENT_CREATION_SCA_UNKNOWN = "consent_creation_sca_unknown.json";

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
                        new DeutscheHeaderValues("", ""),
                        new DeutscheMarketConfiguration(TEST_URL, "PSU_ID_TYPE"));
    }

    @Test
    public void shouldHandleBadRequestInGetConsentCorrectly() {
        // given
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        given(tinkClient.request(new URL(TEST_URL + "/v1/consents")))
                .willThrow(new HttpResponseException(null, mockHttpResponse));
        given(mockHttpResponse.hasBody()).willReturn(true);
        given(mockHttpResponse.getBody(ErrorResponse.class))
                .willReturn(getErrorResponse(CONSENT_CREATION_BAD_REQUEST));
        // when
        Throwable throwable = catchThrowable(() -> apiClient.getConsents("asdf"));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldHandleScaMethodUnknownInGetConsentCorrectly() {
        // given
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        given(tinkClient.request(new URL(TEST_URL + "/v1/consents")))
                .willThrow(new HttpResponseException(null, mockHttpResponse));
        given(mockHttpResponse.hasBody()).willReturn(true);
        given(mockHttpResponse.getBody(ErrorResponse.class))
                .willReturn(getErrorResponse(CONSENT_CREATION_SCA_UNKNOWN));
        // when
        Throwable throwable = catchThrowable(() -> apiClient.getConsents("asdf"));

        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NO_AVAILABLE_SCA_METHODS");
    }

    @Test
    public void shouldThrowHttpResponseExceptionInGetConsentInOtherCases() {
        // given
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        HttpResponseException httpResponseException =
                new HttpResponseException(null, mockHttpResponse);
        given(tinkClient.request(new URL(TEST_URL + "/v1/consents")))
                .willThrow(httpResponseException);
        given(mockHttpResponse.getBody(ErrorResponse.class)).willReturn(new ErrorResponse());
        // when
        Throwable throwable = catchThrowable(() -> apiClient.getConsents("asdf"));

        // then
        assertThat(throwable).isEqualTo(httpResponseException);
    }

    @Test
    public void shouldHandleIncorrectCredentialsInStartAuthorisationCorrectly() {
        // given
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        given(tinkClient.request(new URL(TEST_URL)))
                .willThrow(new HttpResponseException(null, mockHttpResponse));
        given(mockHttpResponse.hasBody()).willReturn(true);
        given(mockHttpResponse.getBody(ErrorResponse.class))
                .willReturn(getErrorResponse(CONSENT_AUTH_CREDENTIALS_INVALID));
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
        given(mockHttpResponse.getBody(ErrorResponse.class)).willReturn(new ErrorResponse());
        // when
        Throwable throwable =
                catchThrowable(
                        () -> apiClient.startAuthorisation(new URL(TEST_URL), "psuid", "password"));

        // then
        assertThat(throwable).isEqualTo(httpResponseException);
    }

    private ErrorResponse getErrorResponse(String filename) {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, filename).toFile(), ErrorResponse.class);
    }
}
