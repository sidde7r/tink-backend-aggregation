package se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankEUConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant.Url.V31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.configuration.UkOpenBankingClientConfigurationAdapter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.enums.MarketCode;

public class DanskebankV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final LocalDateTimeSource localDateTimeSource;

    static {
        aisConfig =
                new DanskebankAisConfiguration.Builder(V31.AIS_BASE, MarketCode.SE)
                        .withWellKnownURL(V31.getWellKnownUrl(MarketCode.SE))
                        .build();
    }

    @Inject
    public DanskebankV31Agent(
            AgentComponentProvider componentProvider, AgentsServiceConfiguration configuration) {
        super(
                componentProvider,
                createEidasJwtSigner(
                        configuration, componentProvider.getContext(), DanskebankV31Agent.class),
                aisConfig,
                true);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    @Override
    protected Class<? extends UkOpenBankingClientConfigurationAdapter>
            getClientConfigurationFormat() {
        return DanskebankEUConfiguration.class;
    }
}
