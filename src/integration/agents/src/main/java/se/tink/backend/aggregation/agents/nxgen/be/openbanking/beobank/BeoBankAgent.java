package se.tink.backend.aggregation.agents.nxgen.be.openbanking.beobank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS})
public final class BeoBankAgent extends CmcicAgent {

    private static final String AUTH_URL_FORMAT =
            "https://online.beobank.be/oauth2/%s/banque/oauth2_authorization.aspx";
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("en", "fr", "nl");
    private static final String PRIMARY_LANGUAGE = "nl";

    @Inject
    public BeoBankAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(
                componentProvider,
                qsealcSigner,
                new CmcicAgentConfig(
                        "https://oauth2-apiii.e-i.com",
                        "/beobank/",
                        getLocalizedAuthUrl(componentProvider)));
    }

    private static String getLocalizedAuthUrl(AgentComponentProvider componentProvider) {
        return String.format(
                AUTH_URL_FORMAT,
                CmcicAgent.getPreferredLanguage(
                        componentProvider.getCredentialsRequest().getUser().getLocale(),
                        SUPPORTED_LANGUAGES,
                        PRIMARY_LANGUAGE));
    }
}
