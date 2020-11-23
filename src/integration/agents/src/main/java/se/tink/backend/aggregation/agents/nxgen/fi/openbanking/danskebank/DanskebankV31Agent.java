package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.mapper.DanskeFiIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskeBankV31EUBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant.Url.V31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class DanskebankV31Agent extends DanskeBankV31EUBaseAgent {

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
        super(componentProvider, configuration, aisConfig, creditCardAccountMapper());
    }

    private static CreditCardAccountMapper creditCardAccountMapper() {
        PrioritizedValueExtractor prioritizedValueExtractor = new PrioritizedValueExtractor();
        return new CreditCardAccountMapper(
                new DefaultCreditCardBalanceMapper(prioritizedValueExtractor),
                new DanskeFiIdentifierMapper(prioritizedValueExtractor));
    }
}
