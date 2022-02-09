package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordeapartner;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

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
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
@AgentDependencyModules(modules = NordeaPartnerKeystoreModule.class)
public final class NordeaPartnerDkAgent extends NordeaPartnerAgent {
    @Inject
    public NordeaPartnerDkAgent(
            AgentComponentProvider agentComponentProvider,
            NordeaPartnerKeystoreProvider keystoreProvider) {
        super(agentComponentProvider, keystoreProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);
    }

    @Override
    protected ZoneId getPaginatorZoneId() {
        return ZoneId.of("Europe/Copenhagen");
    }

    @Override
    protected Collection<String> getSupportedLocales() {
        return ImmutableSet.of("da-DK", "en-DK");
    }
}
