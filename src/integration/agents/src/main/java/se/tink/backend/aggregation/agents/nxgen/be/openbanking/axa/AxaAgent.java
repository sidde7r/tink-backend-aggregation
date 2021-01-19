package se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa.authenticator.AxaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class AxaAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public AxaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api-dailybanking.axabank.be");
    }

    @Override
    protected Xs2aDevelopersAuthenticator constructXs2aAuthenticator(
            AgentComponentProvider componentProvider) {
        return new AxaAuthenticator(
                apiClient,
                persistentStorage,
                configuration,
                componentProvider.getLocalDateTimeSource(),
                credentials);
    }
}
