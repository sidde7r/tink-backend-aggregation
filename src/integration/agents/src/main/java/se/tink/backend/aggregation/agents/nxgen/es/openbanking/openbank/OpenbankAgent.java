package se.tink.backend.aggregation.agents.nxgen.es.openbanking.openbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public class OpenbankAgent extends RedsysAgent {
    @Inject
    public OpenbankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getAspspCode() {
        return "openbank";
    }

    @Override
    public boolean shouldRequestAccountsWithBalance() {
        return true;
    }

    @Override
    public boolean supportsPendingTransactions() {
        return false;
    }

    @Override
    public boolean shouldReturnLowercaseAccountId() {
        return true;
    }

    @Override
    public LocalDate oldestTransactionDate() {
        return LocalDate.now().minusYears(2).plusDays(1);
    }
}
