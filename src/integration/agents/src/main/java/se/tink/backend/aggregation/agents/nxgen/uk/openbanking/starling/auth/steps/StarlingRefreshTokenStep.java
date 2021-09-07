package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.auth.steps;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectAuthenticationRefreshTokenStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectRefreshTokenCallAuthenticationParameters;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessTokenValidator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.TokenExpirationDateHelper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@Slf4j
public class StarlingRefreshTokenStep extends RedirectAuthenticationRefreshTokenStep {

    private final RedirectRefreshTokenCall<?, ?> redirectRefreshTokenCall;

    private final AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
            agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;

    private final RefreshableAccessTokenValidator tokensValidator;

    public StarlingRefreshTokenStep(
            RedirectRefreshTokenCall<?, ?> redirectRefreshTokenCall,
            AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
                    agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory,
            RefreshableAccessTokenValidator tokensValidator) {
        super(
                redirectRefreshTokenCall,
                agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory,
                tokensValidator);
        this.redirectRefreshTokenCall = redirectRefreshTokenCall;
        this.agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory =
                agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
        this.tokensValidator = tokensValidator;
    }

    @SneakyThrows
    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authRequest) {

        AgentAuthenticationPersistedData persistedData =
                authRequest.getAuthenticationPersistedData();
        AgentAuthenticationProcessState authProcessState =
                authRequest.getAuthenticationProcessState();
        AgentExtendedClientInfo clientInfo = authRequest.getAgentExtendedClientInfo();
        AgentRefreshableAccessTokenAuthenticationPersistedData accessTokenPersistedData =
                agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
                        .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                                persistedData);

        return accessTokenPersistedData
                .getRefreshableAccessToken()
                .map(
                        accessToken ->
                                tryToRefreshAccessToken(
                                        accessToken, persistedData, authProcessState, clientInfo))
                .orElseThrow(this::throwSessionExpiredError);
    }

    private AgentAuthenticationResult tryToRefreshAccessToken(
            RefreshableAccessToken accessToken,
            AgentAuthenticationPersistedData persistedData,
            AgentAuthenticationProcessState authProcessState,
            AgentExtendedClientInfo clientInfo) throws SessionException {

        if (isNotRefreshable(accessToken)) {
            throwSessionExpiredError();
        }

        return executeRefreshTokenCall(accessToken, persistedData, authProcessState, clientInfo)
                .filter(this::isRefreshTokenPresent)
                .map(token -> prepareSuccessAuthResult(token, persistedData))
                .orElseThrow(this::throwSessionExpiredError);
    }

    private boolean isNotRefreshable(RefreshableAccessToken refreshableAccessToken) {
        return !tokensValidator.isRefreshable(refreshableAccessToken);
    }

    private boolean isRefreshTokenPresent(RefreshableAccessToken token) {
        return token.getRefreshToken() != null;
    }

    private SessionException throwSessionExpiredError() {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    private AgentAuthenticationResult prepareSuccessAuthResult(
            RefreshableAccessToken token,
            AgentAuthenticationPersistedData authenticationPersistedData) {
        AgentRefreshableAccessTokenAuthenticationPersistedData refreshableAccessTokenPersistedData =
                agentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory
                        .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                                authenticationPersistedData);
        return new AgentSucceededAuthenticationResult(
                TokenExpirationDateHelper.getExpirationDateFromTokenOrDefault(token),
                refreshableAccessTokenPersistedData.storeRefreshableAccessToken(token));
    }

    private Optional<RefreshableAccessToken> executeRefreshTokenCall(
            RefreshableAccessToken currentRefreshableAccessToken,
            AgentAuthenticationPersistedData persistedData,
            AgentAuthenticationProcessState authProcessState,
            AgentExtendedClientInfo clientInfo) {

        RedirectRefreshTokenCallAuthenticationParameters callParams =
                new RedirectRefreshTokenCallAuthenticationParameters(
                        new String(
                                currentRefreshableAccessToken.getRefreshToken().getBody(),
                                StandardCharsets.UTF_8),
                        persistedData,
                        authProcessState);

        ExternalApiCallResult<RefreshableAccessToken> newAccessToken =
                redirectRefreshTokenCall.execute(
                        callParams,
                        clientInfo,
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                persistedData));

        return newAccessToken.getResponse();
    }
}
