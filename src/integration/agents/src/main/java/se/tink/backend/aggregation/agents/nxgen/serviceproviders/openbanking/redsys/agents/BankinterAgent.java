package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.agents;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BankinterTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
public final class BankinterAgent extends RedsysAgent {

    public BankinterAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
    }

    @Override
    public String getAspspCode() {
        return "bankinter";
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
        return LocalDate.now().minusMonths(18).plusDays(1);
    }

    @Override
    public Class<? extends BaseTransactionsResponse> getTransactionsResponseClass() {
        return BankinterTransactionsResponse.class;
    }
}
