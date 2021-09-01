package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.agents;

import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.RedsysGlobalConsentGenerator;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.module.RedsysModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities(generateFromImplementedExecutors = true)
@AgentDependencyModulesForProductionMode(modules = {RedsysModule.class})
public final class SandboxAgent extends RedsysAgent {

    public SandboxAgent(
            AgentComponentProvider componentProvider,
            RedsysGlobalConsentGenerator consentGenerator) {
        super(componentProvider, consentGenerator);
    }

    @Override
    public String getAspspCode() {
        return "redsys";
    }

    @Override
    public boolean shouldRequestAccountsWithBalance() {
        return true;
    }

    @Override
    public boolean supportsPendingTransactions() {
        return false;
    }
}
