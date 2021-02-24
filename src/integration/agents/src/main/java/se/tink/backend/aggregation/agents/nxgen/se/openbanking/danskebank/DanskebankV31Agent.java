package se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank.mapper.DanskeSeIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskeBankV31EUBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.DanskebankV31Constant.Url.V31;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration.DanskebankAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper.DanskeCreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.mapper.DanskeTransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.signer.DanskeOpenBankingModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.transactionalaccounts.TransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModulesForProductionMode(modules = DanskeOpenBankingModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class DanskebankV31Agent extends DanskeBankV31EUBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                new DanskebankAisConfiguration.Builder(V31.AIS_BASE, MarketCode.SE)
                        .withWellKnownURL(V31.getWellKnownUrl(MarketCode.SE))
                        .build();
    }

    @Inject
    public DanskebankV31Agent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(
                componentProvider,
                jwtSigner,
                aisConfig,
                getCreditCardAccountMapperWithDanskeIdentifierMapper(),
                getTransactionalAccountMapperWithDanskeIdentifierMapper());
    }

    private static CreditCardAccountMapper getCreditCardAccountMapperWithDanskeIdentifierMapper() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        return new DanskeCreditCardAccountMapper(
                getCreditCardBalanceMapper(valueExtractor),
                new DanskeSeIdentifierMapper(valueExtractor));
    }

    private static TransactionalAccountMapper
            getTransactionalAccountMapperWithDanskeIdentifierMapper() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        return new DanskeTransactionalAccountMapper(
                new TransactionalAccountBalanceMapper(valueExtractor),
                new DanskeSeIdentifierMapper(valueExtractor));
    }
}
