package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.partner;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Collection;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystoreModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.encryption.NordeaPartnerKeystoreProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
@AgentDependencyModules(modules = NordeaPartnerKeystoreModule.class)
public final class NordeaPartnerFiAgent extends NordeaPartnerAgent {
    @Inject
    public NordeaPartnerFiAgent(
            AgentComponentProvider agentComponentProvider,
            NordeaPartnerKeystoreProvider keystoreProvider) {
        super(agentComponentProvider, keystoreProvider);
    }

    @Override
    protected ZoneId getPaginatorZoneId() {
        return ZoneId.of("Europe/Helsinki");
    }

    @Override
    protected Collection<String> getSupportedLocales() {
        return ImmutableSet.of("fi-FI", "sv-FI", "en-FI");
    }
}
