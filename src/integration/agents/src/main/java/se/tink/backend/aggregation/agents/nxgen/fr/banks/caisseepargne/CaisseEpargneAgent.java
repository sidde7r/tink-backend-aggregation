package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.CaisseEpargneAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.CaisseEpargneTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.uuid.UUIDUtils;

public class CaisseEpargneAgent extends NextGenerationAgent {

    private final CaisseEpargneApiClient apiClient;

    public CaisseEpargneAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {

        super(request, context, signatureKeyPair);

        apiClient = new CaisseEpargneApiClient(client);

        String deviceId = persistentStorage.get(CaisseEpargneConstants.StorageKey.DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUIDUtils.generateUUID();
            persistentStorage.put(CaisseEpargneConstants.StorageKey.DEVICE_ID, deviceId);
        }
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new CaisseEpargneAuthenticator(apiClient, persistentStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        CaisseEpargneTransactionalAccountFetcher fetcher =
                new CaisseEpargneTransactionalAccountFetcher(apiClient);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        fetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(fetcher))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CaisseEpargneSessionHandler(apiClient);
    }
}
