package se.tink.backend.aggregation.agents.nxgen.dk.banks.sjaellandfyn;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

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
public class SjaellandFynAgent extends BankDataAgent {

    @Inject
    public SjaellandFynAgent(
            AgentComponentProvider componentProvider,
            NemIdIFrameControllerInitializer iFrameControllerInitializer) {
        super(componentProvider, iFrameControllerInitializer);
    }

    @Override
    protected BankDataConfiguration createConfiguration() {
        return new SjaellandFynConfiguration();
    }
}
