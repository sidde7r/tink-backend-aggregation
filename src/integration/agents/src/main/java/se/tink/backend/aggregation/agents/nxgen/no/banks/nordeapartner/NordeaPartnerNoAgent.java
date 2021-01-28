package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapartner;

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
public final class NordeaPartnerNoAgent extends NordeaPartnerAgent {

    @Inject
    public NordeaPartnerNoAgent(
            AgentComponentProvider agentComponentProvider,
            NordeaPartnerKeystoreProvider keystoreProvider) {
        super(agentComponentProvider, keystoreProvider);
    }

    @Override
    protected ZoneId getPaginatorZoneId() {
        return ZoneId.of("Europe/Oslo");
    }

    @Override
    protected Collection<String> getSupportedLocales() {
        return ImmutableSet.of("no-NO", "nb-NO", "en-NO");
    }
}
