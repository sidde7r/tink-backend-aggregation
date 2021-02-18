package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class KbcConsentValidationCallTest {

    @Mock private AgentHttpClient httpClient;

    private KbcConsentValidationCall objectUnderTest;

    @Before
    public void init() {
        objectUnderTest = new KbcConsentValidationCall(httpClient);
    }

    @Test
    public void shouldParseConsentException() {
        // given
        ResponseEntity httpResponse = mock(ResponseEntity.class);
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
        when(httpResponse.getBody())
                .thenReturn(
                        "{\"tppMessages\":[{\"category\":\"ERROR\",\"code\":\"CONSENT_EXPIRED\"}]}");
        // when
        ExternalApiCallResult apiCallResult = objectUnderTest.parseResponse(httpResponse);
        // then
        assertThat(apiCallResult.getAgentBankApiError().isPresent()).isTrue();
        assertThat(apiCallResult.getAgentBankApiError().get())
                .isExactlyInstanceOf(SessionExpiredError.class);
    }
}
