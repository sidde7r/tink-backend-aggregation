package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26BaseTestStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26ConsentAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent.N26ConsentPersistentData;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokens;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.Token;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@Ignore
public abstract class N26ValidateConsentStepBaseTest extends N26BaseTestStep {
    protected AgentAuthenticationPersistedData preparePersistedDataWithN26Consent(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        N26ConsentAccessor n26ConsentAccessor =
                new N26ConsentAccessor(agentAuthenticationPersistedData, objectMapper);
        N26ConsentPersistentData n26ConsentPersistentData =
                new N26ConsentPersistentData(CONSENT_ID);
        return n26ConsentAccessor.storeN26ConsentPersistentData(n26ConsentPersistentData);
    }

    protected ExternalApiCallResult<ConsentDetailsResponse> prepareSuccessfulApiCallResult(
            boolean valid) {
        ExternalApiCallResult<ConsentDetailsResponse> apiCallResult =
                mock(ExternalApiCallResult.class);
        when(apiCallResult.getAgentBankApiError()).thenReturn(Optional.empty());
        ConsentDetailsResponse consentStatusResponse = mock(ConsentDetailsResponse.class);
        when(consentStatusResponse.isValid()).thenReturn(valid);
        when(consentStatusResponse.getValidUntil()).thenReturn(LocalDate.parse("2021-01-01"));
        when(apiCallResult.getResponse()).thenReturn(Optional.of(consentStatusResponse));
        return apiCallResult;
    }

    protected AgentAuthenticationPersistedData preparePersistedData() {
        AgentAuthenticationPersistedData agentAuthenticationPersistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());
        agentAuthenticationPersistedData =
                preparePersistedDataWithToken(agentAuthenticationPersistedData);
        agentAuthenticationPersistedData =
                preparePersistedDataWithN26Consent(agentAuthenticationPersistedData);
        return agentAuthenticationPersistedData;
    }

    protected AgentAuthenticationPersistedData preparePersistedDataWithToken(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {

        RedirectTokens redirectTokens =
                RedirectTokens.builder()
                        .accessToken(
                                Token.builder()
                                        .body("TOKEN")
                                        .tokenType("token_type")
                                        .expiresIn(600L, 1L)
                                        .build())
                        .build();

        AgentRedirectTokensAuthenticationPersistedData
                agentRedirectTokensAuthenticationPersistedData =
                        new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(
                                        objectMapper)
                                .createAgentRedirectTokensAuthenticationPersistedData(
                                        agentAuthenticationPersistedData);
        return agentRedirectTokensAuthenticationPersistedData.storeRedirectTokens(redirectTokens);
    }
}
