package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.monabanq;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LIST_BENEFICIARIES;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicAgentConfig;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, LIST_BENEFICIARIES})
public final class MonabanqAgent extends CmcicAgent {

    @Inject
    public MonabanqAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new CmcicAgentConfig(
                        "https://oauth2-apisi.e-i.com",
                        "/monabanq/",
                        "https://www.monabanq.fr/oauth2/fr/banque/oauth2_authorization.aspx"));

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);
    }
}
