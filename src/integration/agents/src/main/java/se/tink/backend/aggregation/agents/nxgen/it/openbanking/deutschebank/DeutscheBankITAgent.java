package se.tink.backend.aggregation.agents.nxgen.it.openbanking.deutschebank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public class DeutscheBankITAgent extends DeutscheBankAgent {

    private static final DeutscheMarketConfiguration DEUTSCHE_IT_CONFIGURATION =
            new DeutscheMarketConfiguration(
                    "https://xs2a.db.com/{"
                            + DeutscheBankConstants.Parameters.SERVICE_KEY
                            + "}/IT/DB",
                    "IT_ONLB_DB");

    @Inject
    public DeutscheBankITAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);
    }

    @Override
    protected DeutscheBankApiClient constructApiClient(DeutscheHeaderValues headerValues) {
        return new DeutscheBankITApiClient(
                client,
                persistentStorage,
                headerValues,
                DEUTSCHE_IT_CONFIGURATION,
                randomValueGenerator,
                localDateTimeSource);
    }
}
