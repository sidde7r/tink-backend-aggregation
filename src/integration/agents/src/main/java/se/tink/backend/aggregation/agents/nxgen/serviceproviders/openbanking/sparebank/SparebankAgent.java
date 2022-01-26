package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public class SparebankAgent extends SparebankBaseAgent {

    @Inject
    public SparebankAgent(
            AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(agentComponentProvider, qsealcSigner);
    }
}
