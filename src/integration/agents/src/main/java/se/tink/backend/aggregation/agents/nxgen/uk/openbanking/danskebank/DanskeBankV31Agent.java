package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UKOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.UKOpenbankingV31Executor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank.DanskeBankConstants.Urls.V31;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank.authenticator.DanskeBankAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

public class DanskeBankV31Agent extends UkOpenBankingBaseAgent {

    private static final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;
    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;

    static {
        aisConfig =
                new UKOpenBankingAis.Builder()
                        .withApiBaseURL(V31.AIS_API_URL)
                        .withWellKnownURL(V31.WELL_KNOWN_URL)
                        .withIdentityDataURL(PartyEndpoints.IDENTITY_DATA_ENDPOINT_PARTY)
                        .withAdditionalPermission(
                                PartyEndpoints.partyEndpointsPermissionMap.get(
                                        PartyEndpoints.IDENTITY_DATA_ENDPOINT_PARTY))
                        .build();
    }

    @Inject
    public DanskeBankV31Agent(AgentComponentProvider componentProvider) {
        super(componentProvider, aisConfig, false);
        pisConfig = new UkOpenBankingV31PisConfiguration(V31.PIS_API_URL);
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage, localDateTimeSource);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        DanskeBankAuthenticator authenticator = new DanskeBankAuthenticator(apiClient);
        return createOpenIdFlowWithAuthenticator(authenticator, null);
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
                        randomValueGenerator);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
