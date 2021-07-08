package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BbvaTransactionsResponse;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE
        })
@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
public final class BbvaAgent extends RedsysAgent {
    @Inject
    public BbvaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getAspspCode() {
        return "BBVA";
    }

    @Override
    public boolean shouldRequestAccountsWithBalance() {
        return true;
    }

    @Override
    public boolean supportsPendingTransactions() {
        return true;
    }

    @Override
    public LocalDate oldestTransactionDate() {
        return LocalDate.now().minusYears(2);
    }

    @Override
    public Class<? extends BaseTransactionsResponse> getTransactionsResponseClass() {
        return BbvaTransactionsResponse.class;
    }
}
