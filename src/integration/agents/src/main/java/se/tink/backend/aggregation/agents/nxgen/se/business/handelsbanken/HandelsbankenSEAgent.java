package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.authenticator.HandelsbankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.identity.HandelsbankenSEIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.HandelsbankenSEAccountTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.filters.HandelsbankenSEBankSideErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.filters.HandelsbankenSEContentTypeFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class HandelsbankenSEAgent
        extends HandelsbankenAgent<HandelsbankenSEApiClient, HandelsbankenSEConfiguration>
        implements RefreshCheckingAccountsExecutor, RefreshIdentityDataExecutor {
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public HandelsbankenSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new HandelsbankenSEConfiguration());
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    protected HandelsbankenSEApiClient constructApiClient(
            HandelsbankenSEConfiguration handelsbankenConfiguration) {
        client.addFilter(new HandelsbankenSEContentTypeFilter());
        client.addFilter(new HandelsbankenSEBankSideErrorFilter());
        return new HandelsbankenSEApiClient(client, handelsbankenConfiguration);
    }

    @Override
    protected TypedAuthenticator[] constructAuthenticators(
            HandelsbankenSEApiClient bankClient,
            HandelsbankenSEConfiguration handelsbankenConfiguration,
            HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {

        return new TypedAuthenticator[] {
            new BankIdAuthenticationController<>(
                    supplementalRequester,
                    new HandelsbankenBankIdAuthenticator(
                            bankClient,
                            credentials,
                            handelsbankenPersistentStorage,
                            handelsbankenSessionStorage),
                    persistentStorage,
                    credentials)
        };
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                new HandelsbankenTransactionalAccountFetcher(
                        this.bankClient, this.handelsbankenSessionStorage),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionIndexPaginationController<>(
                                new HandelsbankenSEAccountTransactionPaginator(
                                        this.bankClient, this.handelsbankenSessionStorage))));
    }

    @Override
    protected Optional<TransferController> constructTransferController(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            CompositeAgentContext context) {
        return Optional.empty();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return HandelsbankenSEIdentityFetcher.fetchIdentityData(persistentStorage);
    }
}
