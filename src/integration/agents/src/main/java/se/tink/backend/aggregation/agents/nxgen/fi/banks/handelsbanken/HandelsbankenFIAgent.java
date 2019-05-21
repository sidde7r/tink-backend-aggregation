package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class HandelsbankenFIAgent
        extends HandelsbankenAgent<HandelsbankenFIApiClient, HandelsbankenFIConfiguration>
        implements RefreshIdentityDataExecutor {
    public HandelsbankenFIAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new HandelsbankenFIConfiguration());
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
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController(
            HandelsbankenFIApiClient bankClient,
            HandelsbankenSessionStorage handelsbankenSessionStorage) {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferController> constructTransferController(
            HandelsbankenFIApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            AgentContext context) {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController(
                    HandelsbankenFIApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return Optional.empty();
    }

    @Override
    protected AccountFetcher<CreditCardAccount> constructCreditCardAccountFetcher(
            HandelsbankenFIApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return new HandelsbankenFICreditCardAccountFetcher(client, sessionStorage);
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
    protected TransactionPaginator<CreditCardAccount> constructCreditCardTransactionPaginator(
            HandelsbankenFIApiClient client, HandelsbankenSessionStorage sessionStorage) {
        return new TransactionKeyPaginationController<>(
                new HandelsbankenFICreditCardTransactionFetcher(client, sessionStorage));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return HandelsbankenFIIdentityFetcher.fetchIdentityData(persistentStorage);
    }
}
