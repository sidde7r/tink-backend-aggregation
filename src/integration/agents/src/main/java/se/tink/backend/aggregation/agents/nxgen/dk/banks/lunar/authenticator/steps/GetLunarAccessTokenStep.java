package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.AgentPlatformLunarApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception.LunarAuthenticationExceptionHandler;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class GetLunarAccessTokenStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;
    private final AgentPlatformLunarApiClient apiClient;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {
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
        } catch (HttpResponseException e) {
            log.error("Failed to send NemIdToken and fetch access token", e);
            return new AgentFailedAuthenticationResult(
                    LunarAuthenticationExceptionHandler.toKnownErrorFromResponseOrDefault(
                            e, new AccessTokenFetchingFailureError()),
                    request.getAuthenticationPersistedData());
        }

        if (StringUtils.isBlank(accessTokenResponse.getAccessToken())) {
            return new AgentFailedAuthenticationResult(
                    new AccessTokenFetchingFailureError(),
                    request.getAuthenticationPersistedData());
        }
        authData.setAccessToken(accessTokenResponse.getAccessToken());

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        SignInToLunarStep.class.getSimpleName()),
                request.getAuthenticationProcessState(),
                authDataAccessor.storeData(authData));
    }
}
