package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_authorization_url;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.N26CryptoService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.oauth2.StrongAuthenticationState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@RequiredArgsConstructor
public class N26FetchAuthorizationUrlStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final N26FetchAuthorizationUrlApiCall apiCall;
    private final String clientId;
    private final String redirectUri;
    private final N26CryptoService n26CryptoService;
    private final ObjectMapper objectMapper;

    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authenticationRequest) {

        N26ProcessStateAccessor n26ProcessStateAccessor =
                new N26ProcessStateAccessor(
                        authenticationRequest.getAuthenticationProcessState(), objectMapper);

        N26ProcessStateData n26ProcessStateData = n26ProcessStateAccessor.getN26ProcessStateData();

        String codeVerifier = n26CryptoService.generateCodeVerifier();
        n26ProcessStateData.setCodeVerifier(codeVerifier);

        N26FetchAuthorizationUrlApiCallParameters apiCallParameters =
                N26FetchAuthorizationUrlApiCallParameters.builder()
                        .clientId(clientId)
                        .codeChallenge(n26CryptoService.generateCodeChallenge(codeVerifier))
                        .redirectUri(redirectUri)
                        .state(StrongAuthenticationState.generateUuidWithTinkTag())
                        .build();
        ExternalApiCallResult<URI> executeResult =
                apiCall.execute(
                        apiCallParameters,
                        null,
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                authenticationRequest.getAuthenticationPersistedData()));

        Optional<AgentBankApiError> optionalBankError = executeResult.getAgentBankApiError();
        if (optionalBankError.isPresent()) {
            return new AgentFailedAuthenticationResult(
                    optionalBankError.get(),
                    authenticationRequest.getAuthenticationPersistedData());
        }

        executeResult.getResponse().ifPresent(n26ProcessStateData::setAuthorizationUri);

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        RedirectPreparationRedirectUrlStep.class.getSimpleName()),
                n26ProcessStateAccessor.storeN26ProcessStateData(n26ProcessStateData),
                authenticationRequest.getAuthenticationPersistedData());
    }
}
