package se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental.authenticator.MultiSupplementalAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.multisupplemental.authenticator.MultiSupplementalManualAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class MultiSupplementalDemoAgent extends NextGenerationDemoAgent {

    public MultiSupplementalDemoAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {

    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                context,
                new MultiSupplementalManualAuthenticator(supplementalInformationController, catalog),
                new MultiSupplementalAutoAuthenticator()
        );
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SessionHandler() {
            @Override
            public void logout() {
                // nop.
            }

            @Override
            public void keepAlive() throws SessionException {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        };
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
