package se.tink.backend.aggregation.agents.nxgen.be.openbanking.hellobank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

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
public class HelloBankAgent extends BnpParibasFortisBaseAgent {

    @Inject
    public HelloBankAgent(
            AgentComponentProvider agentComponentProvider, QsealcSigner qsealcSigner) {
        super(
                agentComponentProvider,
                qsealcSigner,
                new BnpParibasFortisBaseBankConfiguration(
                        "https://regulatory.api.hellobank.be",
                        "https://services.hellobank.be/SEPLJ04/sps/oauth/oauth20/authorize"));
    }
}
