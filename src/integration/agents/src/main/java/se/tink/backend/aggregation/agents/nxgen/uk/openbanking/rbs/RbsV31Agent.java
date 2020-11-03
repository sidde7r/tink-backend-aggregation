package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.rbs;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.JwtSignerModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.CreditCardAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.creditcards.RbsGroupCreditCardBalanceMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.UKOpenbankingV31Executor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.rbs.RbsConstants.Urls.V31;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModulesForProductionMode(
        modules = {UkOpenBankingModule.class, JwtSignerModule.class})
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, SAVINGS_ACCOUNTS, IDENTITY_DATA, TRANSFERS})
public final class RbsV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;

    static {
        aisConfig =
                UKOpenBankingAis.builder()
                        .withOrganisationId("0015800000jfwB4AAI")
                        .withApiBaseURL(V31.AIS_API_URL)
                        .withWellKnownURL(V31.PERSONAL_WELL_KNOWN_URL)
                        .build();
    }

    @Inject
    public RbsV31Agent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(
                componentProvider,
                jwtSigner,
                aisConfig,
                new UkOpenBankingV31PisConfiguration(V31.PIS_API_URL));
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        PrioritizedValueExtractor valueExtractor = new PrioritizedValueExtractor();
        CreditCardAccountMapper creditCardAccountMapper =
                new CreditCardAccountMapper(
                        new RbsGroupCreditCardBalanceMapper(),
                        new DefaultIdentifierMapper(valueExtractor));
        return new UkOpenBankingV31Ais(
                aisConfig, persistentStorage, creditCardAccountMapper, localDateTimeSource);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {

        UKOpenbankingV31Executor paymentExecutor =
                new UKOpenbankingV31Executor(
                        softwareStatement,
                        providerConfiguration,
                        apiClient,
                        supplementalInformationHelper,
                        credentials,
                        strongAuthenticationState,
                        randomValueGenerator);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
