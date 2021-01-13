package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.omasp;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.omasp.OmaspConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.SamlinkAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkAgentsConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class OmaspAgent extends SamlinkAgent {

    @Inject
    public OmaspAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(
                componentProvider,
                qsealcSigner,
                new SamlinkAgentsConfiguration(Urls.BASEURL, Urls.BASEOAUTHURL));
    }
}
