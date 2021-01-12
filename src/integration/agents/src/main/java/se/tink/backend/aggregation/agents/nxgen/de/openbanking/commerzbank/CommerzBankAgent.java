package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.authenticator.CommerzbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class CommerzBankAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public CommerzBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://psd2.api.commerzbank.com");
    }

    @Override
    protected Xs2aDevelopersAuthenticator constructXs2aAuthenticator(
            AgentComponentProvider componentProvider) {
        return new CommerzbankAuthenticator(
                apiClient,
                persistentStorage,
                configuration,
                componentProvider.getLocalDateTimeSource(),
                credentials,
                new CommerzbankApiClient(componentProvider.getTinkHttpClient()));
    }
}
