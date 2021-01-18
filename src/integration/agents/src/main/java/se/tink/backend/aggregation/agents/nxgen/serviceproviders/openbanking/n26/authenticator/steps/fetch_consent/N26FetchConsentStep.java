package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.await_user_confirmation.N26AwaitUserConfirmationStep;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccessTokenFetchingFailureError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RequiredArgsConstructor
@Slf4j
public class N26FetchConsentStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final N26FetchConsentApiCall n26FetchConsentApiCall;
    private final ObjectMapper objectMapper;

    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        Optional<String> optionalAccessToken =
                retrieveAccessTokenFromStorage(authenticationProcessRequest);

        if (!optionalAccessToken.isPresent()) {
            return new AgentFailedAuthenticationResult(
                    new AccessTokenFetchingFailureError(),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }

        String accessToken = optionalAccessToken.get();

        ExternalApiCallResult<ConsentResponse> callResult =
                prepareAndExecuteApiCall(authenticationProcessRequest, accessToken);

        Optional<AgentBankApiError> optionalBankError = callResult.getAgentBankApiError();
        if (optionalBankError.isPresent()) {
            log.error("Could not fetch consent");
            return new AgentFailedAuthenticationResult(
                    optionalBankError.get(),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }

        Optional<ConsentResponse> optionalConsentResponse = callResult.getResponse();

        if (!optionalConsentResponse.isPresent()) {
            log.error("Could not find consent response");
            return new AgentFailedAuthenticationResult(
                    new AuthorizationError(),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }

        String consentId = optionalConsentResponse.get().getConsentId();

        N26ConsentPersistentData consentPersistentData = new N26ConsentPersistentData(consentId);
        N26ConsentAccessor n26ConsentAccessor = getN26ConsentAccessor(authenticationProcessRequest);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        N26AwaitUserConfirmationStep.class.getSimpleName()),
                authenticationProcessRequest.getAuthenticationProcessState(),
                n26ConsentAccessor.storeN26ConsentPersistentData(consentPersistentData));
    }

    private N26ConsentAccessor getN26ConsentAccessor(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        return new N26ConsentAccessor(
                authenticationProcessRequest.getAuthenticationPersistedData(), objectMapper);
    }

    private String mapToAccessToken(RefreshableAccessToken redirectTokens) {
        return new String(redirectTokens.getAccessToken().getBody(), StandardCharsets.UTF_8);
    }

    private Optional<String> retrieveAccessTokenFromStorage(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        return new AgentRefreshableAccessTokenAuthenticationPersistedDataAccessorFactory(
                        objectMapper)
                .createAgentRefreshableAccessTokenAuthenticationPersistedData(
                        authenticationProcessRequest.getAuthenticationPersistedData())
                .getRefreshableAccessToken()
                .map(this::mapToAccessToken);
    }

    private ExternalApiCallResult<ConsentResponse> prepareAndExecuteApiCall(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest,
            String accessToken) {
        N26FetchConsentParameters fetchConsentParameters =
                new N26FetchConsentParameters(accessToken);

        return n26FetchConsentApiCall.execute(
                fetchConsentParameters,
                null,
                AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                        authenticationProcessRequest.getAuthenticationPersistedData()));
    }
}
