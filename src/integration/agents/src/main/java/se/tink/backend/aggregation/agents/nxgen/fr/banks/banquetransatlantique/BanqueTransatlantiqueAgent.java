package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS})
public final class BanqueTransatlantiqueAgent extends EuroInformationAgent {
    @Inject
    public BanqueTransatlantiqueAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new BanqueTransatlantiqueConfiguration(),
                new BanqueTransatlantiqueApiClientFactory());
    }
}
