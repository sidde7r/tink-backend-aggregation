package se.tink.backend.aggregation.agents.nxgen.es.openbanking.ing;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {PisCapability.SEPA_CREDIT_TRANSFER, PisCapability.PIS_FUTURE_DATE})
public final class IngAgent extends IngBaseAgent {

    @Inject
    public IngAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    public boolean shouldReturnLowercaseAccountId() {
        // ING Spain RE agent uses lowercase account IDs
        return true;
    }

    @Override
    public LocalDate earliestTransactionHistoryDate() {
        // All transaction information since the payment account was opened
        return LocalDate.now().minusYears(7);
    }
}
