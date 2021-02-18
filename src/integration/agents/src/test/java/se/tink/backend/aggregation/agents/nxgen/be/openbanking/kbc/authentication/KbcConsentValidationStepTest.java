package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RunWith(MockitoJUnitRunner.class)
public class KbcConsentValidationStepTest {
    private static final String CONSENT_ID = "CONSENT_ID";

    @Mock private KbcConsentValidationCall apiCall;
    @Mock private KbcConfiguration configuration;

    private KbcConsentValidationStep objectUnderTest;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectUnderTest = new KbcConsentValidationStep(apiCall, objectMapper, configuration);
    }

    @Test
    public void shouldExecuteSuccessfullyWhenConsentValid() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                prepareSuccessfulRequest();
        ExternalApiCallResult apiCallResult = mock(ExternalApiCallResult.class);
        when(apiCallResult.getAgentBankApiError()).thenReturn(Optional.empty());

        when(apiCall.execute(any(), any(), any())).thenReturn(apiCallResult);
        // when
        AgentAuthenticationResult authenticationResult =
                objectUnderTest.execute(authenticationProcessRequest);
        // then
        assertThat(authenticationResult)
                .isExactlyInstanceOf(AgentSucceededAuthenticationResult.class);
    }

    @Test
    public void shouldMoveToManualStepWhenConsentExpired() {
        // given
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                prepareSuccessfulRequest();
        ExternalApiCallResult apiCallResult = mock(ExternalApiCallResult.class);
        when(apiCallResult.getAgentBankApiError())
                .thenReturn(Optional.of(new SessionExpiredError()));
        when(apiCall.execute(any(), any(), any())).thenReturn(apiCallResult);
        // when
        AgentAuthenticationResult authenticationResult =
                objectUnderTest.execute(authenticationProcessRequest);
        // then
        assertThat(authenticationResult).isExactlyInstanceOf(AgentFailedAuthenticationResult.class);
        assertThat(((AgentFailedAuthenticationResult) authenticationResult).getError())
                .isExactlyInstanceOf(SessionExpiredError.class);
    }

    @Test
    public void shouldReturnErrorStatus() {
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                prepareSuccessfulRequest();
        ExternalApiCallResult apiCallResult = mock(ExternalApiCallResult.class);
        when(apiCallResult.getAgentBankApiError()).thenReturn(Optional.of(new ServerError()));
        when(apiCall.execute(any(), any(), any())).thenReturn(apiCallResult);
        // when
        AgentAuthenticationResult authenticationResult =
                objectUnderTest.execute(authenticationProcessRequest);
        // then
        assertThat(authenticationResult).isExactlyInstanceOf(AgentFailedAuthenticationResult.class);
    }

    private AgentProceedNextStepAuthenticationRequest prepareSuccessfulRequest() {
        AgentProceedNextStepAuthenticationRequest authenticationProcessRequest =
                mock(AgentProceedNextStepAuthenticationRequest.class);
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());

        RefreshableAccessToken redirectTokens =
                RefreshableAccessToken.builder()
                        .accessToken(Token.builder().body("TOKEN").tokenType("token_type").build())
                        .build();
        AgentRefreshableAccessTokenAuthenticationPersistedData
                agentRedirectTokensAuthenticationPersistedData =
                        new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                                        objectMapper)
                                .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                                        agentAuthenticationPersistedData);

        agentAuthenticationPersistedData =
                agentRedirectTokensAuthenticationPersistedData.storeRefreshableAccessToken(
                        redirectTokens);

        KbcPersistedData kbcPersistedData =
                new KbcPersistedData(agentAuthenticationPersistedData, objectMapper);

        KbcAuthenticationData kbcAuthenticationData = new KbcAuthenticationData();
        kbcAuthenticationData.setConsentId(CONSENT_ID);

        agentAuthenticationPersistedData =
                kbcPersistedData.storeKbcAuthenticationData(kbcAuthenticationData);

        when(authenticationProcessRequest.getAuthenticationPersistedData())
                .thenReturn(agentAuthenticationPersistedData);
        return authenticationProcessRequest;
    }
}
