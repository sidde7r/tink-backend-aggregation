package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentRedirectAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.RefreshTokenFailureError;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

@RunWith(JUnitParamsRunner.class)
public class KbcRedirectAuthenticationRefreshTokenStepHandlerTest {

    private final KbcRedirectAuthenticationRefreshTokenStepHandler
            kbcRedirectAuthenticationRefreshTokenStepHandler =
                    new KbcRedirectAuthenticationRefreshTokenStepHandler();

    private AgentAuthenticationResult authenticationResult;

    @Mock
    private AgentProceedNextStepAuthenticationRequest agentProceedNextStepAuthenticationRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldThrowExceptionWhenTokenRefreshFailedAndUserNotAvailableForInteraction() {
        // given
        authenticationResult = agentNonSucceededAuthenticationResult();

        // when
        AgentAuthenticationResult tokenRefreshResult =
                kbcRedirectAuthenticationRefreshTokenStepHandler.defineResultOfAccessTokenRefresh(
                        agentProceedNextStepAuthenticationRequest, authenticationResult, false);

        // expect
        assertThatAccessTokenWasNotRefreshedSuccessfully(tokenRefreshResult);
    }

    @Test
    @Parameters(method = "prepareDataForUnsuccessfulTokenRefreshThatTriggersNextAuthenticationStep")
    public void shouldProceedToTheNextStepWhenTokenNotRefreshed(
            AgentAuthenticationResult authenticationResult, boolean userAvailableForInteraction) {
        // when
        AgentAuthenticationResult tokenRefreshResult =
                kbcRedirectAuthenticationRefreshTokenStepHandler.defineResultOfAccessTokenRefresh(
                        agentProceedNextStepAuthenticationRequest,
                        authenticationResult,
                        userAvailableForInteraction);

        // expect
        assertThatTokenWasNotRefreshedAndNextStepWillFollow(tokenRefreshResult);
    }

    @SuppressWarnings("unused")
    private Object[] prepareDataForUnsuccessfulTokenRefreshThatTriggersNextAuthenticationStep() {
        AgentRedirectAuthenticationResult agentRedirectAuthenticationResult =
                agentRedirectAuthenticationResult();

        return new Object[][] {
            {agentRedirectAuthenticationResult, true},
            {agentRedirectAuthenticationResult, false},
            {agentNonSucceededAuthenticationResult(), true}
        };
    }

    @Test
    public void shouldProceedToNextStepWhenTokenRefreshSuccessful() {
        // given
        authenticationResult = agentSucceededAuthenticationResult();

        // when
        AgentAuthenticationResult tokenRefreshResult =
                kbcRedirectAuthenticationRefreshTokenStepHandler.defineResultOfAccessTokenRefresh(
                        agentProceedNextStepAuthenticationRequest, authenticationResult, true);

        // expect
        assertThat(accessTokenIsSuccessfullyRefreshed(tokenRefreshResult)).isTrue();
    }

    private void assertThatAccessTokenWasNotRefreshedSuccessfully(
            AgentAuthenticationResult tokenRefreshResult) {
        // expect
        assertThat(tokenRefreshResult).isExactlyInstanceOf(AgentFailedAuthenticationResult.class);

        // and
        AgentFailedAuthenticationResult agentFailedAuthenticationResult =
                (AgentFailedAuthenticationResult) tokenRefreshResult;
        AgentBankApiError errorThrown = agentFailedAuthenticationResult.getError();
        assertThat(errorThrown).isExactlyInstanceOf(RefreshTokenFailureError.class);

        // and
        Error errorDetails = errorThrown.getDetails();
        assertThat(errorDetails.getErrorMessage())
                .contains(
                        "For safety reasons you have been logged out. Please login again to continue.");
        assertThat(errorDetails.getErrorCode()).contains("APAG-5");

        // and
        assertThat(new AgentAuthenticationPersistedData(new HashMap<>()))
                .isEqualTo(tokenRefreshResult.getAuthenticationPersistedData());
    }

    private void assertThatTokenWasNotRefreshedAndNextStepWillFollow(
            AgentAuthenticationResult tokenRefreshResult) {
        // expect
        assertThat(tokenRefreshResult).isNotInstanceOf(AgentFailedAuthenticationResult.class);

        // and
        assertThat(accessTokenIsSuccessfullyRefreshed(tokenRefreshResult)).isFalse();
    }

    private boolean accessTokenIsSuccessfullyRefreshed(
            AgentAuthenticationResult tokenRefreshResult) {
        Optional<AgentAuthenticationProcessStepIdentifier> stepIdentifier =
                tokenRefreshResult.getAuthenticationProcessStepIdentifier();

        return tokenRefreshResult instanceof AgentProceedNextStepAuthenticationResult
                && stepIdentifier.isPresent()
                && stepIdentifier
                        .get()
                        .equals(
                                AgentAuthenticationProcessStep.identifier(
                                        KbcConsentValidationStep.class));
    }

    private AgentSucceededAuthenticationResult agentSucceededAuthenticationResult() {
        return new AgentSucceededAuthenticationResult(agentAuthenticationPersistedData());
    }

    private AgentProceedNextStepAuthenticationResult agentNonSucceededAuthenticationResult() {
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStep.identifier(RedirectPreparationRedirectUrlStep.class),
                agentAuthenticationProcessState(),
                agentAuthenticationPersistedData());
    }

    private AgentRedirectAuthenticationResult agentRedirectAuthenticationResult() {
        return new AgentRedirectAuthenticationResult(
                "redirectUrl.com",
                AgentAuthenticationProcessStep.identifier(KbcConsentValidationStep.class),
                agentAuthenticationPersistedData(),
                agentAuthenticationProcessState());
    }

    private AgentAuthenticationProcessState agentAuthenticationProcessState() {
        return AgentAuthenticationProcessState.of("key-state", "value-state");
    }

    private AgentAuthenticationPersistedData agentAuthenticationPersistedData() {
        return AgentAuthenticationPersistedData.of("key1", "value1");
    }
}
