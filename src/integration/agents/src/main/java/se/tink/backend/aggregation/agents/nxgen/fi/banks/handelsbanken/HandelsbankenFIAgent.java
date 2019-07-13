package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.authenticator.HandelsbankenFICardDeviceAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.HandelsbankenFIIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.HandelsbankenFICreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.creditcard.HandelsbankenFICreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenFIAccountTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class HandelsbankenFIAgent
        extends HandelsbankenAgent<HandelsbankenFIApiClient, HandelsbankenFIConfiguration>
        implements RefreshIdentityDataExecutor, RefreshCreditCardAccountsExecutor {

    private final CreditCardRefreshController creditCardRefreshController;

    public HandelsbankenFIAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new HandelsbankenFIConfiguration());

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected HandelsbankenFIApiClient constructApiClient(
            HandelsbankenFIConfiguration handelsbankenConfiguration) {
        return new HandelsbankenFIApiClient(this.client, handelsbankenConfiguration);
    }

    @Override
    protected TypedAuthenticator[] constructAuthenticators(
            HandelsbankenFIApiClient bankClient,
            HandelsbankenFIConfiguration handelsbankenConfiguration,
            HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {
        return new TypedAuthenticator[] {
            constructAutoAuthenticationController(
                    new HandelsbankenFICardDeviceAuthenticator(
                            bankClient,
                            handelsbankenPersistentStorage,
                            new SupplementalInformationController(
                                    this.supplementalRequester, this.credentials),
                            handelsbankenConfiguration,
                            new HandelsbankenAutoAuthenticator(
                                    bankClient,
                                    handelsbankenPersistentStorage,
                                    this.credentials,
                                    handelsbankenSessionStorage,
                                    handelsbankenConfiguration)))
        };
    }

    @Override
    protected Optional<TransferController> constructTransferController(
            HandelsbankenFIApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            AgentContext context) {
        return Optional.empty();
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                new HandelsbankenFICreditCardAccountFetcher(
                        this.bankClient, this.handelsbankenSessionStorage),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new HandelsbankenFICreditCardTransactionFetcher(
                                        this.bankClient, this.handelsbankenSessionStorage))));
    }

    @Override
    protected TransactionPaginator<TransactionalAccount> constructAccountTransactionPaginator(
            HandelsbankenFIApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return new TransactionKeyPaginationController<>(
                new HandelsbankenFIAccountTransactionPaginator(client, sessionStorage));
    }

    @Override
    protected UpcomingTransactionFetcher<TransactionalAccount> constructUpcomingTransactionFetcher(
            HandelsbankenFIApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return null;
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return HandelsbankenFIIdentityFetcher.fetchIdentityData(persistentStorage);
    }
}
