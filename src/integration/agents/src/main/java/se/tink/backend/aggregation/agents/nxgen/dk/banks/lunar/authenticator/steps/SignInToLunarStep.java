package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception.LunarAuthExceptionHandler;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;

@RequiredArgsConstructor
@Slf4j
public class SignInToLunarStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;
    private final AuthenticationApiClient apiClient;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        LunarAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        LunarAuthData authData = authDataAccessor.get();

        LunarProcessState processState =
                dataAccessorFactory
                        .createProcessStateAccessor(request.getAuthenticationProcessState())
                        .get();

        String lunarPassword = authData.getLunarPassword();
        String token = authData.getAccessToken();
        String deviceId = authData.getDeviceId();
        boolean isAutoAuth = processState.isAutoAuth();

        TokenResponse tokenResponse;

        try {
            tokenResponse = apiClient.signIn(lunarPassword, token, deviceId);
        } catch (ResponseStatusException e) {
            return LunarAuthExceptionHandler.getSignInFailedAuthResult(
                    authDataAccessor, e, isAutoAuth);
        }

        if (StringUtils.isBlank(tokenResponse.getToken())) {
            log.error("Token in the response from Lunar is empty!");
            return new AgentFailedAuthenticationResult(
                    getDefaultError(isAutoAuth), authDataAccessor.clearData());
        }

        setNewAccessTokenIfDifferent(authData, token, tokenResponse);

        return new AgentSucceededAuthenticationResult(authDataAccessor.storeData(authData));
    }

    private void setNewAccessTokenIfDifferent(
            LunarAuthData authData, String token, TokenResponse tokenResponse) {
        if (!token.equals(tokenResponse.getToken())) {
            log.warn("Token received from Lunar is different than the one in storage!");
            authData.setAccessToken(tokenResponse.getToken());
        }
    }

    private AgentBankApiError getDefaultError(boolean isAutoAuth) {
        if (isAutoAuth) {
            return new SessionExpiredError();
        }
        return new AccessTokenFetchingFailureError();
    }
}
