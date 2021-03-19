package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26ConsentAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26ConsentPersistentData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.rpc.ValidateConsentCombinedResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RequiredArgsConstructor
@Slf4j
public abstract class N26ValidateConsentBaseStep {
    protected final N26ValidateConsentApiCall n26ValidateConsentApiCall;
    protected final ObjectMapper objectMapper;

    protected Token accessToken;

    protected abstract AgentAuthenticationResult parseResponseToResult(
            ExternalApiCallResult<ValidateConsentCombinedResponse> callResult,
            AgentAuthenticationPersistedData persistedData,
            AgentAuthenticationProcessState processState);

    protected Optional<AgentAuthenticationResult> parseCommonResponseToResult(
            ExternalApiCallResult<ValidateConsentCombinedResponse> callResult,
            AgentAuthenticationPersistedData persistedData) {

        Optional<AgentBankApiError> optionalBankError = callResult.getAgentBankApiError();
        if (optionalBankError.isPresent()) {
            log.error("Could not fetch consent");
            return Optional.of(new AgentFailedAuthenticationResult(optionalBankError.get(), null));
        }

        if (callResult.getResponse().isPresent()) {
            return parseValidateConsentCombinedResponse(
                    callResult.getResponse().get(), persistedData);
        }
        log.warn("SessionExpired: Could not validate consent");
        return Optional.of(
                new AgentFailedAuthenticationResult(new SessionExpiredError(), persistedData));
    }

    protected ExternalApiCallResult<ValidateConsentCombinedResponse> buildAndExecuteRequest(
            AgentAuthenticationPersistedData persistedData) {

        accessToken = getAccessToken(persistedData);
        String accessTokenValue = new String(accessToken.getBody(), StandardCharsets.UTF_8);

        N26ConsentPersistentData n26ConsentPersistentData =
                getN26ConsentPersistentData(persistedData);

        if (n26ConsentPersistentData.getConsentId() == null) {
            log.warn("SessionExpired: ConsentId was null");
            return new ExternalApiCallResult<>(new SessionExpiredError());
        }

        N26ValidateConsentParameters n26ValidateConsentParameters =
                N26ValidateConsentParameters.builder()
                        .consentId(n26ConsentPersistentData.getConsentId())
                        .accessToken(accessTokenValue)
                        .build();

        return n26ValidateConsentApiCall.execute(
                n26ValidateConsentParameters,
                null,
                AuthenticationPersistedDataCookieStoreAccessorFactory.create(persistedData));
    }

    private N26ConsentPersistentData getN26ConsentPersistentData(
            AgentAuthenticationPersistedData persistedData) {
        N26ConsentAccessor n26ConsentAccessor = getN26ConsentAccessor(persistedData);
        return n26ConsentAccessor.getN26ConsentPersistentData();
    }

    private N26ConsentAccessor getN26ConsentAccessor(
            AgentAuthenticationPersistedData persistedData) {
        return new N26ConsentAccessor(persistedData, objectMapper);
    }

    private Token getAccessToken(AgentAuthenticationPersistedData persistedData) {
        return new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                        objectMapper)
                .createAgentRefreshableAccessTokenAuthenticationPersistedData(persistedData)
                .getRefreshableAccessToken()
                .get()
                .getAccessToken();
    }

    private Optional<AgentAuthenticationResult> parseValidateConsentCombinedResponse(
            ValidateConsentCombinedResponse callResultResponse,
            AgentAuthenticationPersistedData persistedData) {
        if (callResultResponse.hasValidDetails()) {
            ConsentDetailsResponse consentDetailsResponse = callResultResponse.getValidResponse();
            Instant validUntil =
                    consentDetailsResponse
                            .getValidUntil()
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant();
            return Optional.of(new AgentSucceededAuthenticationResult(validUntil, persistedData));

        } else if (callResultResponse.isLoginExpired()) {
            log.info("SessionExpired: Consent expired");
            return Optional.of(
                    new AgentFailedAuthenticationResult(new SessionExpiredError(), persistedData));
        }
        return Optional.empty();
    }
}
