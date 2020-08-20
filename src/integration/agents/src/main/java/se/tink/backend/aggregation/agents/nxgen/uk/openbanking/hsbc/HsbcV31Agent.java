package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForDecoupledMode;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModulesForProductionMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingLocalKeySignerModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.module.UkOpenBankingLocalKeySignerModuleForDecoupledMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.UKOpenbankingV31Executor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.HsbcConstants.Urls.V313;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

@AgentDependencyModulesForProductionMode(modules = UkOpenBankingLocalKeySignerModule.class)
@AgentDependencyModulesForDecoupledMode(
        modules = UkOpenBankingLocalKeySignerModuleForDecoupledMode.class)
public class HsbcV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;
    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;

    static {
        aisConfig =
                new UKOpenBankingAis.Builder()
                        .withApiBaseURL(V313.PERSONAL_AIS_API_URL)
                        .withWellKnownURL(V313.PERSONAL_WELL_KNOWN_URL)
                        .withAppToAppURL(V313.PERSONAL_APP_TO_APP_AUTH_URL)
                        .withIdentityDataURL(PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY)
                        .withAdditionalPermission(
                                PartyEndpoints.partyEndpointsPermissionMap.get(
                                        PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY))
                        .build();
    }

    @Inject
    public HsbcV31Agent(AgentComponentProvider componentProvider, JwtSigner jwtSigner) {
        super(componentProvider, jwtSigner, aisConfig, false);
        pisConfig = new UkOpenBankingV31PisConfiguration(V313.PERSONAL_PIS_API_URL);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        UKOpenbankingV31Executor paymentExecutor =
                new UKOpenbankingV31Executor(
                        pisConfig,
                        softwareStatement,
                        providerConfiguration,
                        apiClient,
                        supplementalInformationHelper,
                        credentials,
                        strongAuthenticationState,
                        randomValueGenerator,
                        aisConfig.getAppToAppURL());
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
