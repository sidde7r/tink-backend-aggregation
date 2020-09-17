package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskeBankV31EUBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant.Url.V31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.enums.MarketCode;

public class DanskebankV31Agent extends DanskeBankV31EUBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                new DanskebankAisConfiguration.Builder(V31.AIS_BASE, MarketCode.FI)
                        .withWellKnownURL(V31.getWellKnownUrl(MarketCode.FI))
                        .build();
    }

    @Inject
    public DanskebankV31Agent(
            AgentComponentProvider componentProvider, AgentsServiceConfiguration configuration) {
        super(componentProvider, configuration, aisConfig);
    }
}
