package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.BancoBpiAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.transaction.BancoBpiTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.transaction.BancoBpiTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoBpiAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    private StatelessProgressiveAuthenticator authenticator;
    private TransactionalAccountRefreshController transactionalAccountRefreshController;
    private BancoBpiEntityManager entityManager;

    public BancoBpiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new BancoBpiAuthenticator(
                            client,
                            new SupplementalInformationFormer(request.getProvider()),
                            getEntityManager());
        }
        return authenticator;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return getTransactionalAccountRefreshController().fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return getTransactionalAccountRefreshController().fetchCheckingTransactions();
    }

    @Override
    public SteppableAuthenticationResponse login(SteppableAuthenticationRequest request)
            throws Exception {
        SteppableAuthenticationResponse response = super.login(request);
        getEntityManager().saveEntities();
        return response;
    }

    private BancoBpiEntityManager getEntityManager() {
        if (entityManager == null) {
            entityManager = new BancoBpiEntityManager(persistentStorage, sessionStorage);
        }
        return entityManager;
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        if (transactionalAccountRefreshController == null) {
            transactionalAccountRefreshController =
                    new TransactionalAccountRefreshController(
                            metricRefreshController,
                            updateController,
                            new BancoBpiTransactionalAccountFetcher(client, getEntityManager()),
                            new BancoBpiTransactionFetcher());
        }
        return transactionalAccountRefreshController;
    }
}
