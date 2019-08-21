package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.agents;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BankiaTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BankiaAgent extends RedsysAgent {

    public BankiaAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    public String getAspspCode() {
        return "bankia";
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
    public LocalDate oldestTransactionDate() {
        return LocalDate.now().minusMonths(23).withDayOfMonth(1);
    }

    @Override
    public Class<? extends BaseTransactionsResponse> getTransactionsResponseClass() {
        return BankiaTransactionsResponse.class;
    }
}
