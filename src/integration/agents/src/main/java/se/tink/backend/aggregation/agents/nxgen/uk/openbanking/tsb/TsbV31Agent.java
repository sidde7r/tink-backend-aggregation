package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tsb;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingESSBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31AisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.UKOpenbankingV31Executor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tsb.TsbConstants.Urls.V31;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class TsbV31Agent extends UkOpenBankingESSBaseAgent {
    private static final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;

    static {
        aisConfig =
                new UkOpenBankingV31AisConfiguration.Builder()
                        .withApiBaseURL(V31.AIS_API_URL)
                        .withWellKnownURL(V31.WELL_KNOWN_URL)
                        .build();
    }

    public TsbV31Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, aisConfig);
        pisConfig = new UkOpenBankingV31PisConfiguration(V31.PIS_API_URL);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig, persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }

    @Override
    protected void configureAisHttpClient(TinkHttpClient httpClient) {}

    @Override
    protected void configurePisHttpClient(TinkHttpClient httpClient) {}

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
                        strongAuthenticationState);
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
