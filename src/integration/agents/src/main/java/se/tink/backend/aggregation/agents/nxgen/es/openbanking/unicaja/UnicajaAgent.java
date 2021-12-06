package se.tink.backend.aggregation.agents.nxgen.es.openbanking.unicaja;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.consent.ConsentGenerator;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.BaseTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc.EactParsingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.module.RedsysModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
@AgentDependencyModulesForProductionMode(modules = {RedsysModule.class})
public final class UnicajaAgent extends RedsysAgent {
    @Inject
    public UnicajaAgent(
            AgentComponentProvider componentProvider,
            ConsentGenerator<ConsentRequestBody> consentGenerator) {
        super(componentProvider, consentGenerator);
    }

    @Override
    public String getAspspCode() {
        return "unicajabanco";
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
    public Class<? extends BaseTransactionsResponse> getTransactionsResponseClass() {
        return EactParsingTransactionsResponse.class;
    }
}
