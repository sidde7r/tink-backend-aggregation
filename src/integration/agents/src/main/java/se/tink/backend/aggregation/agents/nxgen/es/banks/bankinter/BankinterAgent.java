package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.authenticator.BankinterAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.identitydata.BankinterIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.BankinterTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.session.BankinterSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class BankinterAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor {

    private final BankinterApiClient apiClient;

    public BankinterAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new BankinterApiClient(client, persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new BankinterAuthenticator(apiClient, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final BankinterTransactionalAccountFetcher accountFetcher =
                new BankinterTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(accountFetcher))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BankinterSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new BankinterIdentityDataFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
