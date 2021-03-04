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
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;

@RequiredArgsConstructor
@Slf4j
public class GetLunarAccessTokenStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;
    private final AuthenticationApiClient apiClient;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        LunarAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        LunarAuthData authData = authDataAccessor.get();

        LunarProcessStateAccessor processStateAccessor =
                dataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());
        LunarProcessState processState = processStateAccessor.get();

        String signature = processState.getNemIdToken();
        String challenge = processState.getChallenge();
        String deviceId = authData.getDeviceId();

        AccessTokenResponse accessTokenResponse;

        try {
            accessTokenResponse = apiClient.postNemIdToken(signature, challenge, deviceId);
        } catch (ResponseStatusException e) {
            log.error("Failed to send NemIdToken and fetch access token", e);
            return new AgentFailedAuthenticationResult(
                    LunarAuthExceptionHandler.toKnownErrorFromResponseOrDefault(
                            e, new AccessTokenFetchingFailureError()),
                    authDataAccessor.clearData());
        }

        if (StringUtils.isBlank(accessTokenResponse.getAccessToken())) {
            return new AgentFailedAuthenticationResult(
                    new AccessTokenFetchingFailureError(), authDataAccessor.clearData());
        }
        authData.setAccessToken(accessTokenResponse.getAccessToken());
        processState.setAutoAuth(false);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStep.identifier(FetchAccountsToConfirmLoginStep.class),
                processStateAccessor.storeState(processState),
                authDataAccessor.storeData(authData));
    }
}
