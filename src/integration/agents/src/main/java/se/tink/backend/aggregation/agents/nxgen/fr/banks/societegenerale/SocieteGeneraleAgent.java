package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.authenticator.SocieteGeneraleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.SocieteGeneraleTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.uuid.UUIDUtils;

public class SocieteGeneraleAgent extends NextGenerationAgent {

    private final SocieteGeneraleApiClient apiClient;

    public SocieteGeneraleAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new SocieteGeneraleApiClient(client, persistentStorage, sessionStorage);
        checkDeviceId();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new SocieteGeneraleAuthenticator(apiClient, persistentStorage, sessionStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        SocieteGeneraleTransactionalAccountFetcher fetcher =
                new SocieteGeneraleTransactionalAccountFetcher(apiClient);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        fetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(fetcher, 0))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SocieteGeneraleSessionHandler(apiClient);
    }

    private void checkDeviceId() {

        String deviceId = persistentStorage.get(SocieteGeneraleConstants.StorageKey.DEVICE_ID);

        if (deviceId == null) {
            deviceId = UUIDUtils.generateUUID();
            persistentStorage.put(SocieteGeneraleConstants.StorageKey.DEVICE_ID, deviceId);
        }
    }
}
