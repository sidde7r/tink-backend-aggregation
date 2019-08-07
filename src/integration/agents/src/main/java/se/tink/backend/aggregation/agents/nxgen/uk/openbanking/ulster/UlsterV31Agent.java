package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ulster;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAis;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingPisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31AisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.UkOpenBankingV31PisConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.UKOpenbankingV31Executor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ulster.UlsterConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ulster.UlsterConstants.Urls.V31;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UlsterV31Agent extends UkOpenBankingBaseAgent {
    private final UkOpenBankingAisConfig aisConfig;
    private final UkOpenBankingPisConfig pisConfig;

    public UlsterV31Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new URL(V31.WELL_KNOWN_URL));
        aisConfig = new UkOpenBankingV31AisConfiguration(V31.AIS_API_URL, V31.AIS_AUTH_URL);
        pisConfig = new UkOpenBankingV31PisConfiguration(V31.PIS_API_URL, V31.PIS_AUTH_URL);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return super.constructAuthenticator(aisConfig);
    }

    @Override
    protected UkOpenBankingAis makeAis() {
        return new UkOpenBankingV31Ais(aisConfig);
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
                        client,
                        new URL(Urls.V31.WELL_KNOWN_URL),
                        supplementalInformationHelper,
                        credentials,
                        configuration.getCallbackJwtSignatureKeyPair());
        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
