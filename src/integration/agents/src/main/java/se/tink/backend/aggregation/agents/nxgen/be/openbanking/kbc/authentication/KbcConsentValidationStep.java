package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RequiredArgsConstructor
@Slf4j
public class KbcConsentValidationStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {
    private final KbcConsentValidationCall apiCall;
    private final ObjectMapper objectMapper;
    private final KbcConfiguration configuration;

    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {

        Token accessToken =
                getAccessToken(authenticationProcessRequest.getAuthenticationPersistedData());
        String accessTokenValue = new String(accessToken.getBody(), StandardCharsets.UTF_8);

        KbcAuthenticationData kbcAuthenticationData =
                getKbcAuthenticationData(
                        authenticationProcessRequest.getAuthenticationPersistedData());

        KbcConsentValidationParameters kbcConsentValidationParameters =
                prepareCallParams(kbcAuthenticationData, accessTokenValue);

        ExternalApiCallResult<String> callResult =
                apiCall.execute(
                        kbcConsentValidationParameters,
                        null,
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                authenticationProcessRequest.getAuthenticationPersistedData()));

        Optional<AgentBankApiError> optionalBankError = callResult.getAgentBankApiError();
        return optionalBankError
                .map(
                        agentBankApiError ->
                                handleAgentBankApiError(
                                        agentBankApiError, authenticationProcessRequest))
                .orElseGet(() -> getSucceededResult(accessToken, authenticationProcessRequest));
    }

    private KbcConsentValidationParameters prepareCallParams(
            KbcAuthenticationData authenticationPersistedData, String accessTokenValue) {

        return new KbcConsentValidationParameters(
                authenticationPersistedData.getConsentId(),
                accessTokenValue,
                configuration.getPsuIpAddress());
    }

    private Token getAccessToken(AgentAuthenticationPersistedData authenticationPersistedData) {
        return new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                        objectMapper)
                .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                        authenticationPersistedData)
                .getRefreshableAccessToken()
                .get()
                .getAccessToken();
    }

    private KbcAuthenticationData getKbcAuthenticationData(
            AgentAuthenticationPersistedData authenticationPersistedData) {
        KbcPersistedData kbcPersistedData =
                new KbcPersistedData(authenticationPersistedData, objectMapper);
        return kbcPersistedData.getKbcAuthenticationData();
    }

    private AgentAuthenticationResult handleAgentBankApiError(
            AgentBankApiError error,
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        return new AgentFailedAuthenticationResult(
                error, authenticationProcessRequest.getAuthenticationPersistedData());
    }

    private AgentAuthenticationResult getSucceededResult(
            Token accessToken,
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        Optional<Instant> expirationDate = accessToken.getExpirationDate();
        return expirationDate
                .map(
                        instant ->
                                new AgentSucceededAuthenticationResult(
                                        instant,
                                        authenticationProcessRequest
                                                .getAuthenticationPersistedData()))
                .orElseGet(
                        () ->
                                new AgentSucceededAuthenticationResult(
                                        authenticationProcessRequest
                                                .getAuthenticationPersistedData()));
    }
}
