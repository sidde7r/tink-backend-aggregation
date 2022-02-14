package se.tink.agent.compatibility_layers.aggregation_service.authentication;

import se.tink.agent.compatibility_layers.aggregation_service.authentication.report.AuthenticationReport;
import se.tink.agent.runtime.authentication.RuntimeAuthenticationApi;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.authentication.consent.ConsentStatus;
import se.tink.agent.sdk.authentication.steppable_execution.ExistingConsentFlow;
import se.tink.agent.sdk.authentication.steppable_execution.NewConsentFlow;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import src.agent_sdk.compatibility_layers.aggregation_service.src.steppable_execution.AggregationServiceSteppableExecutor;

public class Authentication {
    private final AggregationServiceSteppableExecutor steppableExecutor;
    private final RuntimeAuthenticationApi runtimeAuthenticationApi;

    public Authentication(
            SupplementalInformationController supplementalInformationController,
            AgentInstance agentInstance) {
        this.steppableExecutor =
                new AggregationServiceSteppableExecutor(
                        supplementalInformationController,
                        agentInstance.getOperation().getAgentStorage());

        this.runtimeAuthenticationApi = new RuntimeAuthenticationApi(agentInstance);
    }

    public AuthenticationReport initiate(boolean issueNewConsent) {
        ConsentLifetime consentLifetime = null;

        if (issueNewConsent) {
            NewConsentFlow newConsentFlow = runtimeAuthenticationApi.getNewConsentFlow();
            consentLifetime = this.steppableExecutor.execute(newConsentFlow, null);
            // fallthrough and always execute use existing consent flow
        }

        ExistingConsentFlow existingConsentFlow = runtimeAuthenticationApi.getExistingConsentFlow();
        ConsentStatus consentStatus = this.steppableExecutor.execute(existingConsentFlow, null);

        return new AuthenticationReport(consentStatus, consentLifetime);
    }
}
