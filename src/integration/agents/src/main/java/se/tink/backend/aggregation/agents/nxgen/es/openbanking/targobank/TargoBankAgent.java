package se.tink.backend.aggregation.agents.nxgen.es.openbanking.targobank;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities(generateFromImplementedExecutors = true)
public final class TargoBankAgent extends CmcicAgent {

    private static final String AUTH_URL_FORMAT =
            "https://www.targobank.es/oauth2/%s/banque/oauth2_authorization.aspx";
    private static final Map<String, String> SUPPORTED_LANGUAGES = new HashMap<>();
    private static final String PRIMARY_LANGUAGE = "es";

    // Targobank requires "cat" which is ISO639-3 code for Catalan,
    // while all the other languages for Targo and other CMCIC banks are in ISO639-1 format.
    // Therefore, this beautiful hack is introduced.
    static {
        SUPPORTED_LANGUAGES.put("ca", "cat");
        SUPPORTED_LANGUAGES.put("es", "es");
        SUPPORTED_LANGUAGES.put("fr", "fr");
    }

    @Inject
    public TargoBankAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(
                componentProvider,
                qsealcSigner,
                new CmcicAgentConfig(
                        "https://oauth2-apiii.e-i.com",
                        "/targobank-es/",
                        getLocalizedAuthUrl(componentProvider)));
    }

    private static String getLocalizedAuthUrl(AgentComponentProvider componentProvider) {
        String preferredLanguage =
                CmcicAgent.getPreferredLanguage(
                        componentProvider.getCredentialsRequest().getUser().getLocale(),
                        SUPPORTED_LANGUAGES.keySet(),
                        PRIMARY_LANGUAGE);
        return String.format(AUTH_URL_FORMAT, SUPPORTED_LANGUAGES.get(preferredLanguage));
    }
}
