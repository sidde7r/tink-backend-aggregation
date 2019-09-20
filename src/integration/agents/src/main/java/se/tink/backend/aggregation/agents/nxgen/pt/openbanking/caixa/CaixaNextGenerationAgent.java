package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsBaseNextGenerationAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsDecoupledAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.SibsPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategyFactory;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CaixaNextGenerationAgent extends SibsBaseNextGenerationAgent {

    public CaixaNextGenerationAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return CaixaConstants.INTEGRATION_NAME;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final SibsDecoupledAuthenticationController controller =
                new SibsDecoupledAuthenticationController(
                        new SibsAuthenticator(apiClient, credentials),
                        supplementalInformationHelper);
        authenticator =
                new AutoAuthenticationController(request, systemUpdater, controller, controller);
        return authenticator;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SignPaymentStrategy signPaymentStrategy =
                SignPaymentStrategyFactory.buildSignPaymentDecoupledStrategy(
                        apiClient, credentials);
        SibsPaymentExecutor sibsPaymentExecutor =
                new SibsPaymentExecutor(apiClient, signPaymentStrategy, strongAuthenticationState);
        return Optional.of(new PaymentController(sibsPaymentExecutor, sibsPaymentExecutor));
    }
}
