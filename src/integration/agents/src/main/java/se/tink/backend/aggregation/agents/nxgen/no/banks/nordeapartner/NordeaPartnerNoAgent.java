package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapartner;

import com.google.common.collect.ImmutableSet;
import java.time.ZoneId;
import java.util.Collection;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class NordeaPartnerNoAgent extends NordeaPartnerAgent {
    public NordeaPartnerNoAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration);
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
