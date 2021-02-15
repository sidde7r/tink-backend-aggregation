package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.hellobank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasBankConfig;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA, TRANSFERS, CREDIT_CARDS})
@AgentPisCapability(capabilities = PisCapability.PIS_SEPA)
public final class HelloBankAgent extends BnpParibasBaseAgent {

    private static final String AUTHORIZE_URL =
            "https://api-nav-psd2.bddf.bnpparibas/as/psd2/hellobank/authorize";
    private static final String TOKEN_URL =
            "https://api-psd2.bddf.bnpparibas/as/psd2/hellobank/token";
    private static final String BASE_URL = "https://api-psd2.bddf.bnpparibas/psd2/hellobank/V1.4";

    @Inject
    public HelloBankAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(
                componentProvider,
                qsealcSigner,
                new BnpParibasBankConfig(AUTHORIZE_URL, TOKEN_URL, BASE_URL));
    }
}
