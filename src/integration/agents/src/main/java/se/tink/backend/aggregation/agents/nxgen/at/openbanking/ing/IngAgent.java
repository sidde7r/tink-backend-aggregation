package se.tink.backend.aggregation.agents.nxgen.at.openbanking.ing;

import com.google.inject.Inject;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class IngAgent extends IngBaseAgent {

    @Inject
    public IngAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    public LocalDate earliestTransactionHistoryDate() {
        return LocalDate.now().minusYears(7);
    }
}
