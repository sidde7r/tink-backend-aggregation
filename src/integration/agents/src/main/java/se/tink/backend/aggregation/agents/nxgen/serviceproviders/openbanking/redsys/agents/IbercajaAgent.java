package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.agents;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IbercajaAgent extends RedsysAgent {

    public IbercajaAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    public String getAspspCode() {
        return "ibercaja";
    }

    @Override
    public boolean shouldRequestAccountsWithBalance() {
        return false;
    }

    @Override
    public boolean supportsPendingTransactions() {
        return false;
    }

    @Override
    public LocalDate oldestTransactionDate() {
        return LocalDate.now().minusMonths(24).withDayOfMonth(1);
    }
}
