package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.partner;

import com.google.common.collect.ImmutableSet;
import java.time.ZoneId;
import java.util.Collection;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NordeaPartnerFiAgent extends NordeaPartnerAgent {
    public NordeaPartnerFiAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
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
