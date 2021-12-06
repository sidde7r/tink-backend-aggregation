package se.tink.backend.aggregation.agents.nxgen.dk.banks.sydbank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializerModule;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, IDENTITY_DATA})
@AgentDependencyModules(modules = NemIdIFrameControllerInitializerModule.class)
public class SydbankAgent extends BankDataAgent {

    @Inject
    public SydbankAgent(
            AgentComponentProvider componentProvider,
            NemIdIFrameControllerInitializer iFrameControllerInitializer) {
        super(componentProvider, iFrameControllerInitializer);
    }

    @Override
    protected BankDataConfiguration createConfiguration() {
        return new SydbankConfiguration();
    }
}
