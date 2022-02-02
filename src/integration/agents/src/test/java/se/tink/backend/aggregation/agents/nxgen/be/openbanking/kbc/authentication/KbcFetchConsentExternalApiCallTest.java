package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(JUnitParamsRunner.class)
public class KbcFetchConsentExternalApiCallTest {

    @Mock private AgentHttpClient httpClient;
    @Mock private ResponseEntity<String> httpResponse;

    private final KbcFetchConsentExternalApiCall fetchConsentApiCall =
            new KbcFetchConsentExternalApiCall(httpClient, Urls.BASE_URL);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldThrowWhenBankRespondsWithFormatError() {
        // given
        httpResponse =
                bankRespondsWith(
                        400,
                        "{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"FORMAT_ERROR\"}]}");

        // when
        ExternalApiCallResult<String> apiCallResult =
                fetchConsentApiCall.parseResponse(httpResponse);

        // then
        assertThat(apiCallResult.getAgentBankApiError())
                .containsInstanceOf(InvalidCredentialsError.class);
        assertThat(apiCallResult.getAgentBankApiError())
                .map(bankApiError -> bankApiError.getDetails().getErrorCode())
                .hasValue(AgentError.INVALID_CREDENTIALS.getCode());
        assertThat(apiCallResult.getAgentBankApiError())
                .map(bankApiError -> bankApiError.getDetails().getErrorMessage())
                .hasValue("Incorrect login credentials. Please try again.");
    }

    @Test
    @Parameters()
    public void shouldThrowAuthorizationErrorOnGivenStatusAndBody(
            int statusCode, String responseBody) {
        // given
        httpResponse = bankRespondsWith(statusCode, responseBody);

        // when
        ExternalApiCallResult<String> apiCallResult =
                fetchConsentApiCall.parseResponse(httpResponse);

        // then
        assertThat(apiCallResult.getAgentBankApiError())
                .containsInstanceOf(AuthorizationError.class);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldThrowAuthorizationErrorOnGivenStatusAndBody() {
        return new Object[][] {
            {400, null},
            {401, null}
        };
    }

    @Test
    public void shouldThrowAuthenticationErrorWhenBankRespondsWithInvalidConsent() {
        // given
        httpResponse =
                bankRespondsWith(
                        401,
                        "{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"CONSENT_INVALID\"}]}");

        // when
        ExternalApiCallResult<String> apiCallResult =
                fetchConsentApiCall.parseResponse(httpResponse);

        // then
        assertThat(apiCallResult.getAgentBankApiError())
                .containsInstanceOf(AuthenticationError.class);
        assertThat(apiCallResult.getAgentBankApiError())
                .map(bankApiError -> bankApiError.getDetails().getErrorCode())
                .hasValue(AgentError.INVALID_CREDENTIALS.getCode());
        assertThat(apiCallResult.getAgentBankApiError())
                .map(bankApiError -> bankApiError.getDetails().getErrorMessage())
                .hasValue(ErrorCodes.CONSENT_INVALID);
    }

    @Test
    @Parameters()
    public void shouldThrowServerErrorWhenBankRespondsWithServerError(int statusCode) {
        // given
        httpResponse = bankRespondsWith(statusCode, null);

        // when
        ExternalApiCallResult<String> apiCallResult =
                fetchConsentApiCall.parseResponse(httpResponse);

        // then
        assertThat(apiCallResult.getAgentBankApiError()).containsInstanceOf(ServerError.class);
        assertThat(apiCallResult.getAgentBankApiError())
                .map(bankApiError -> bankApiError.getDetails().getErrorCode())
                .hasValue(AgentError.HTTP_RESPONSE_ERROR.getCode());
        assertThat(apiCallResult.getAgentBankApiError())
                .map(bankApiError -> bankApiError.getDetails().getErrorMessage())
                .hasValue(AgentError.HTTP_RESPONSE_ERROR.getMessage());
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldThrowServerErrorWhenBankRespondsWithServerError() {
        return new Object[] {500, 501, 502, 503, 504, 505, 506, 507, 508, 509, 510, 511};
    }

    private ResponseEntity<String> bankRespondsWith(int statusCode, String responseBody) {
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.valueOf(statusCode));
        when(httpResponse.getBody()).thenReturn(responseBody);
        return httpResponse;
    }
}
