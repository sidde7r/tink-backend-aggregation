package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.executor.payment.KbcPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class KbcAgent extends BerlinGroupAgent<KbcApiClient, BerlinGroupConfiguration> {
    private KbcApiClient apiClient;

    public KbcAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        Credentials credentials = request.getCredentials();
        apiClient = new KbcApiClient(client, sessionStorage, credentials);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new KbcAuthenticator(apiClient),
                credentials);
    }

    @Override
    protected void setupClient(TinkHttpClient client) {
        client.setSslClientCertificate(
                BerlinGroupUtils.readFile(getConfiguration().getClientKeyStorePath()),
                getConfiguration().getClientKeyStorePassword());
    }

    @Override
    protected KbcApiClient getApiClient() {
        return apiClient;
    }

    @Override
    protected String getIntegrationName() {
        return KbcConstants.INTEGRATION_NAME;
    }

    @Override
    protected Class<BerlinGroupConfiguration> getConfigurationClassDescription() {
        return BerlinGroupConfiguration.class;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        BerlinGroupPaymentAuthenticator paymentAuthenticator =
                new BerlinGroupPaymentAuthenticator(supplementalInformationHelper);

        KbcPaymentExecutor kbcPaymentExecutor =
                new KbcPaymentExecutor(apiClient, paymentAuthenticator, getConfiguration());

        return Optional.of(new PaymentController(kbcPaymentExecutor, kbcPaymentExecutor));
    }
}
