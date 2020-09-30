package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentRedirectAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectUrlBuilder;
import se.tink.backend.aggregation.api.Psd2Headers;

public class KbcRedirectPreparationRedirectUrlStep extends RedirectPreparationRedirectUrlStep {

    private final KbcPersistedDataAccessorFactory kbcPersistedDataAccessorFactory;

    public KbcRedirectPreparationRedirectUrlStep(
            RedirectUrlBuilder redirectUrlBuilder,
            KbcPersistedDataAccessorFactory kbcPersistedDataAccessorFactory) {
        super(redirectUrlBuilder);
        this.kbcPersistedDataAccessorFactory = kbcPersistedDataAccessorFactory;
    }

    @Override
    public AgentRedirectAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest request) {
        storeCodeVerifierInPersistedData(
                request.getAuthenticationPersistedData(), Psd2Headers.generateCodeVerifier());
        return super.execute(request);
    }

    private void storeCodeVerifierInPersistedData(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData,
            String codeVerifier) {
        KbcPersistedData persistedData =
                kbcPersistedDataAccessorFactory.createKbcAuthenticationPersistedDataAccessor(
                        agentAuthenticationPersistedData);
        KbcAuthenticationData authenticationData = persistedData.getKbcAuthenticationData();
        authenticationData.setCodeVerifier(codeVerifier);
        persistedData.storeKbcAuthenticationData(authenticationData);
    }
}
