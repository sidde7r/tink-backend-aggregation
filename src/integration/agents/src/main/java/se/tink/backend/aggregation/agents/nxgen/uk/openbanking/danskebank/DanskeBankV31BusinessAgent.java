package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingConstants.PartyEndpoint;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingFlowModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingAisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards.DefaultCreditCardBalanceMapper;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModulesForProductionMode(modules = UkOpenBankingFlowModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, IDENTITY_DATA})
public class DanskeBankV31BusinessAgent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;

    static {
        aisConfig =
                UkOpenBankingAisConfiguration.builder()
                        .withOrganisationId(DanskeBankConstants.ORGANISATION_ID)
                        .withApiBaseURL(DanskeBankConstants.AIS_API_URL)
                        .withWellKnownURL(DanskeBankConstants.BUSINESS_WELL_KNOWN_URL)
                        .withPartyEndpoints(PartyEndpoint.PARTY)
                        .build();
    }

    @Inject
    public DanskeBankV31BusinessAgent(
            AgentComponentProvider componentProvider, UkOpenBankingFlowFacade flowFacade) {
        super(componentProvider, flowFacade, aisConfig);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        CreditCardAccountMapper creditCardAccountMapper =
                new CreditCardAccountMapper(
                        new DefaultCreditCardBalanceMapper(valueExtractor),
                        new DanskeBankIdentifierMapper(valueExtractor));
        return new UkOpenBankingV31Ais(
                aisConfig,
                persistentStorage,
                localDateTimeSource,
                creditCardAccountMapper,
                UkOpenBankingV31Ais.defaultTransactionalAccountMapper());
    }
}
