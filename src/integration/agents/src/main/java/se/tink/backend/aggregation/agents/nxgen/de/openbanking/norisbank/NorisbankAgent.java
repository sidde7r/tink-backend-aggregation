package se.tink.backend.aggregation.agents.nxgen.de.openbanking.norisbank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public final class NorisbankAgent extends DeutscheBankAgent {
    private static final DeutscheMarketConfiguration NORIS_CONFIGURATION =
            new DeutscheMarketConfiguration("https://xs2a.db.com/ais/DE/Noris", "DE_ONLB_NORIS");

    @Inject
    public NorisbankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected DeutscheBankApiClient constructApiClient(DeutscheHeaderValues headerValues) {
        return new NorisbankApiClient(client, persistentStorage, headerValues, NORIS_CONFIGURATION);
    }
}
