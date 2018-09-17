package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.CaisseEpargneAuthenticator;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
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
import se.tink.backend.common.config.SignatureKeyPair;
import se.tink.libraries.uuid.UUIDUtils;

public class CaisseEpargneAgent extends NextGenerationAgent {

    private final CaisseEpargneApiClient apiClient;

    public CaisseEpargneAgent(CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new CaisseEpargneApiClient(client);
        checkDeviceId();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
       // client.setProxy("http://127.0.0.1:8888");
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new CaisseEpargneAuthenticator(apiClient, persistentStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CaisseEpargneSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    private void checkDeviceId() {

        String deviceId = persistentStorage.get(CaisseEpargneConstants.StorageKey.DEVICE_ID);

        if (deviceId == null) {
            deviceId = UUIDUtils.generateUUID();
            persistentStorage.put(CaisseEpargneConstants.StorageKey.DEVICE_ID, deviceId);
        }
    }

}
