package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.cic;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LIST_BENEFICIARIES;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    LIST_BENEFICIARIES,
    TRANSFERS,
    CREDIT_CARDS
})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.SEPA_CREDIT_TRANSFER
        })
public final class CicAgent extends CmcicAgent {

    private static final String AUTH_URL_FORMAT =
            "https://www.cic.fr/oauth2/%s/banque/oauth2_authorization.aspx";
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList("de", "en", "es", "fr");
    private static final String PRIMARY_LANGUAGE = "fr";

    @Inject
    public CicAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new CmcicAgentConfig(
                        "https://oauth2-apiii.e-i.com",
                        "/cic/",
                        getLocalizedAuthUrl(componentProvider)));

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);
    }

    private static String getLocalizedAuthUrl(AgentComponentProvider componentProvider) {
        return String.format(
                AUTH_URL_FORMAT,
                CmcicAgent.getPreferredLanguage(
                        componentProvider.getUser().getLocale(),
                        SUPPORTED_LANGUAGES,
                        PRIMARY_LANGUAGE));
    }
}
