package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.AgentPlatformLunarApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractSignInToLunarStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;
    private final AgentPlatformLunarApiClient apiClient;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        LunarAuthDataAccessor persistedData =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        LunarAuthData authData = persistedData.get();

        String lunarPassword = authData.getLunarPassword();
        String token = authData.getAccessToken();
        String deviceId = authData.getDeviceId();

        TokenResponse tokenResponse;

        try {
            tokenResponse = apiClient.signIn(lunarPassword, token, deviceId);
        } catch (HttpResponseException e) {
            return getFailedAuthResult(request, e);
        }

        if (StringUtils.isBlank(tokenResponse.getToken())) {
            log.error("Token in the response from Lunar is empty!");
            return new AgentFailedAuthenticationResult(
                    getDefaultError(), request.getAuthenticationPersistedData());
        }

        setNewAccessTokenIfHasChanged(authData, token, tokenResponse);

        return new AgentSucceededAuthenticationResult(persistedData.storeData(authData));
    }

    private void setNewAccessTokenIfHasChanged(
            LunarAuthData authData, String token, TokenResponse tokenResponse) {
        if (!token.equals(tokenResponse.getToken())) {
            log.warn("Token received from Lunar is different than the one in storage!");
            authData.setAccessToken(tokenResponse.getToken());
        }
    }

    abstract AgentFailedAuthenticationResult getFailedAuthResult(
            AgentProceedNextStepAuthenticationRequest request, HttpResponseException e);

    abstract AgentBankApiError getDefaultError();
}
