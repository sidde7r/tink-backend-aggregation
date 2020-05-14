package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.partner;

import com.google.common.collect.ImmutableSet;
import java.time.ZoneId;
import java.util.Collection;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.mapper.NordeaPartnerAccountMapper;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NordeaPartnerSeAgent extends NordeaPartnerAgent {

    public NordeaPartnerSeAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
    }

    @Override
    protected NordeaPartnerAccountMapper getAccountMapper() {
        if (accountMapper == null) {
            accountMapper = new NordeaPartnerSeAccountMapper();
        }
        return accountMapper;
    }

    @Override
    protected ZoneId getPaginatorZoneId() {
        return ZoneId.of("Europe/Stockholm");
    }

    @Override
    protected Collection<String> getSupportedLocales() {
        return ImmutableSet.of("sv-SE", "en-SE");
    }
}
