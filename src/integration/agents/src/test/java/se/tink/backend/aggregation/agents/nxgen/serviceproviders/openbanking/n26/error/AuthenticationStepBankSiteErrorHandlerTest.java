package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.error;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerTemporaryUnavailableError;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationStepBankSiteErrorHandlerTest {

    @Mock private N26BankSiteErrorDiscoverer errorDiscoverer;

    @Mock private HttpClientException httpClientException;

    @Mock private AgentAuthenticationRequest agentAuthenticationRequest;

    @Mock private AgentAuthenticationPersistedData agentAuthenticationPersistedData;

    @Before
    public void init() {
        Mockito.when(agentAuthenticationRequest.getAuthenticationPersistedData())
                .thenReturn(agentAuthenticationPersistedData);
    }

    @Test
    public void shouldHandleErrorAndReturnAgentFailedAuthemticationResult() {
        // given;
        final String errorMessage = "error message";
        Mockito.when(httpClientException.getMessage()).thenReturn(errorMessage);
        Mockito.when(errorDiscoverer.isBankSiteError(httpClientException)).thenReturn(true);
        AuthenticationStepBankSiteErrorHandler objectUnderTest =
                new AuthenticationStepBankSiteErrorHandler(errorDiscoverer) {
                    @Override
                    protected AgentAuthenticationResult execute(
                            AgentAuthenticationRequest authenticationProcessRequest) {
                        throw httpClientException;
                    }
                };

        // when
        AgentAuthenticationResult result =
                objectUnderTest.executeWithHandling(agentAuthenticationRequest);

        // then
        Assertions.assertThat(result.getAuthenticationPersistedData())
                .isEqualTo(agentAuthenticationPersistedData);
        Assertions.assertThat(result).isInstanceOf(AgentFailedAuthenticationResult.class);
        AgentBankApiError error = ((AgentFailedAuthenticationResult) result).getError();
        Assertions.assertThat(error).isInstanceOf(ServerTemporaryUnavailableError.class);
        Assertions.assertThat(error.getDetails().getErrorMessage()).isEqualTo(errorMessage);
    }

    @Test
    public void shouldReturnStepResult() {
        // given;
        AgentAuthenticationResult stepResult =
                Mockito.mock(AgentProceedNextStepAuthenticationResult.class);
        AuthenticationStepBankSiteErrorHandler objectUnderTest =
                new AuthenticationStepBankSiteErrorHandler(errorDiscoverer) {
                    @Override
                    protected AgentAuthenticationResult execute(
                            AgentAuthenticationRequest authenticationProcessRequest) {
                        return stepResult;
                    }
                };

        // when
        AgentAuthenticationResult result =
                objectUnderTest.executeWithHandling(agentAuthenticationRequest);

        // then
        Assertions.assertThat(result).isEqualTo(result);
    }
}
