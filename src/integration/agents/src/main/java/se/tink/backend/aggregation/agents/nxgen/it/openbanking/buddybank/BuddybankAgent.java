package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.BuddybankConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.BuddybankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.BuddybankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor.BuddybankPaymentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.UnicreditPaymentExecutor;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BuddybankAgent extends UnicreditBaseAgent {

    public BuddybankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return Market.INTEGRATION_NAME;
    }

    @Override
    protected UnicreditBaseApiClient getApiClient(boolean manualRequest) {
        return new BuddybankApiClient(client, persistentStorage, credentials, manualRequest);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BuddybankAuthenticationController(
                new BuddybankAuthenticator((BuddybankApiClient) apiClient),
                strongAuthenticationState,
                supplementalRequester);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {

        return Optional.of(
                new BuddybankPaymentController(
                        new UnicreditPaymentExecutor(apiClient),
                        (BuddybankApiClient) apiClient,
                        persistentStorage));
    }
}
