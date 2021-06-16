package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditmutuel;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LIST_BENEFICIARIES;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA, TRANSFERS, LIST_BENEFICIARIES, CREDIT_CARDS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE
        })
public final class CreditMutuelAgent extends CmcicAgent {

    private static final String AUTH_URL_FORMAT =
            "https://www.creditmutuel.fr/oauth2/%s/banque/oauth2_authorization.aspx";
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("de", "en", "es", "fr");
    private static final String PRIMARY_LANGUAGE = "fr";

    @Inject
    public CreditMutuelAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(
                componentProvider,
                qsealcSigner,
                new CmcicAgentConfig(
                        "https://oauth2-apisi.e-i.com",
                        "/cm/",
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
