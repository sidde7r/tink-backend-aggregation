package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.configuration.BnpParibasFortisBaseBankConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS})
public class BnpParibasFortisAgent extends BnpParibasFortisBaseAgent {

    @Inject
    public BnpParibasFortisAgent(
            AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(
                agentComponentProvider,
                qsealcSigner,
                new BnpParibasFortisBaseBankConfiguration(
                        "https://regulatory.api.bnpparibasfortis.be",
                        "https://services.bnpparibasfortis.be/SEPLJ04/sps/oauth/oauth20/authorize"));
    }
}
