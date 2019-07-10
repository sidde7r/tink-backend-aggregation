package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.HandelsbankenNOAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.HandelsbankenNOMultiFactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.HandelsbankenNOInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenNOAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenNOTransactionFetcher;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class HandelsbankenNOAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor {

    private final HandelsbankenNOApiClient apiClient;
    private EncapClient encapClient;
    private final InvestmentRefreshController investmentRefreshController;

    public HandelsbankenNOAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new HandelsbankenNOApiClient(client, sessionStorage);
        encapClient =
                new EncapClient(
                        new HandelsbankenNOEncapConfiguration(),
                        persistentStorage,
                        client,
                        true,
                        credentials.getField(Field.Key.USERNAME));

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new HandelsbankenNOInvestmentFetcher(
                                apiClient, credentials.getField(Field.Key.USERNAME)));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        HandelsbankenNOMultiFactorAuthenticator multiFactorAuthenticator =
                new HandelsbankenNOMultiFactorAuthenticator(
                        apiClient,
                        sessionStorage,
                        supplementalInformationController,
                        catalog,
                        encapClient);

        HandelsbankenNOAutoAuthenticator autoAuthenticator =
                new HandelsbankenNOAutoAuthenticator(apiClient, encapClient, sessionStorage);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new BankIdAuthenticationControllerNO(
                        supplementalRequester, multiFactorAuthenticator),
                autoAuthenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new HandelsbankenNOAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionIndexPaginationController<>(
                                        new HandelsbankenNOTransactionFetcher(apiClient)))));
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new HandelsbankenNOSessionHandler(apiClient);
    }

    public void populateSessionStorage(String key, String value) {
        this.sessionStorage.put(key, value);
    }
}
