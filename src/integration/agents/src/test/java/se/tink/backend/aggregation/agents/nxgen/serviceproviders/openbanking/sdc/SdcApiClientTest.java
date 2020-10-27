package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SdcApiClientTest {

    private static final String TOKEN = "/Token";
    private static final String INTERNAL_ERROR_JSON_STRING =
            "{\"error\":\"internal_error\",\"error_description\":\"Internal error in Participantmanager (internalservererror)\"}";
    private static final String OTHER_ERROR_JSON_STRING =
            "{\"error\":\"bad_request\",\"error_description\":\"Request is invalid\"}";

    @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    private SdcApiClient apiClient;

    @Before
    public void setup() {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder().build(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();

        apiClient =
                new SdcApiClient(
                        httpClient,
                        new SdcUrlProvider(wireMock.baseUrl(), wireMock.baseUrl()),
                        null,
                        new SdcConfiguration(),
                        null);
    }

    @Test
    public void shouldRetryThreeTimesAndThenFailIfBanksInternalErrorEncountered() {
        // given
        stubTokenRefreshWith(INTERNAL_ERROR_JSON_STRING);

        // when
        Throwable throwable = catchThrowable(() -> apiClient.refreshAccessToken("any"));

        // then
        assertThat(throwable)
                .isInstanceOf(BankServiceException.class)
                .hasMessage("Cause: BankServiceError.BANK_SIDE_FAILURE");
        verify(3, postRequestedFor(urlEqualTo(TOKEN)));
    }

    @Test
    public void shouldNotRetryAndEndUpAsGenericExceptionInOtherErrorCases() {
        // given
        stubTokenRefreshWith(OTHER_ERROR_JSON_STRING);

        // when
        Throwable throwable = catchThrowable(() -> apiClient.refreshAccessToken("any"));

        // then
        assertThat(throwable).isInstanceOf(HttpResponseException.class);
        verify(postRequestedFor(urlEqualTo(TOKEN)));
    }

    private void stubTokenRefreshWith(String responseBody) {
        WireMock.stubFor(
                WireMock.post(urlEqualTo(TOKEN))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(400)
                                        .withBody(responseBody)
                                        .withHeader("Content-Type", "application/json")));
    }
}
