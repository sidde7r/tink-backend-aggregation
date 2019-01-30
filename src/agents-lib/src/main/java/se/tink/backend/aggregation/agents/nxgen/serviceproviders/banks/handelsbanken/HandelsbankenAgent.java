package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan.HandelsbankenLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public abstract class HandelsbankenAgent<API extends HandelsbankenApiClient, Config extends HandelsbankenConfiguration>
        extends NextGenerationAgent {

    private final API bankClient;
    private final HandelsbankenPersistentStorage handelsbankenPersistentStorage;
    private final HandelsbankenSessionStorage handelsbankenSessionStorage;
    private final Config handelsbankenConfiguration;

    public HandelsbankenAgent(CredentialsRequest request, AgentContext context,
            SignatureKeyPair signatureKeyPair,
            Config handelsbankenConfiguration) {
        super(request, context, signatureKeyPair);
        this.handelsbankenConfiguration = handelsbankenConfiguration;
        this.handelsbankenPersistentStorage = new HandelsbankenPersistentStorage(
                this.persistentStorage, credentials.getSensitivePayload());
        this.bankClient = constructApiClient(handelsbankenConfiguration);
        this.handelsbankenSessionStorage = new HandelsbankenSessionStorage(this.sessionStorage,
                handelsbankenConfiguration);

    }

    protected abstract API constructApiClient(Config handelsbankenConfiguration);

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                constructAuthenticators(this.bankClient, this.handelsbankenConfiguration,
                        this.handelsbankenPersistentStorage,
                        this.handelsbankenSessionStorage)
        );
    }

    protected abstract TypedAuthenticator[] constructAuthenticators(API bankClient,
            Config handelsbankenConfiguration,
            HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage);

    protected AutoAuthenticationController constructAutoAuthenticationController(
            MultiFactorAuthenticator cardDeviceAuthenticator,
            HandelsbankenAutoAuthenticator autoAuthenticator) {
        return new AutoAuthenticationController(this.request, this.context,
                cardDeviceAuthenticator, autoAuthenticator);
    }

    protected AutoAuthenticationController constructAutoAuthenticationController(
            MultiFactorAuthenticator
                    cardDeviceAuthenticator) {
        return constructAutoAuthenticationController(cardDeviceAuthenticator,
                constructAutoAuthenticator());
    }

    protected HandelsbankenAutoAuthenticator constructAutoAuthenticator() {
        return new HandelsbankenAutoAuthenticator(this.bankClient,
                this.handelsbankenPersistentStorage,
                this.credentials,
                this.handelsbankenSessionStorage, this.handelsbankenConfiguration);
    }

    protected abstract Optional<InvestmentRefreshController> constructInvestmentRefreshController(
            API bankClient,
            HandelsbankenSessionStorage handelsbankenSessionStorage);

    protected abstract Optional<EInvoiceRefreshController> constructEInvoiceRefreshController(
            API client,
            HandelsbankenSessionStorage sessionStorage);

    protected abstract Optional<TransferController> constructTransferController(API client,
            HandelsbankenSessionStorage sessionStorage, AgentContext context);

    protected abstract Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController(
            API client,
            HandelsbankenSessionStorage sessionStorage);

    protected abstract AccountFetcher<CreditCardAccount> constructCreditCardAccountFetcher(
            API client,
            HandelsbankenSessionStorage sessionStorage
    );

    protected abstract TransactionPaginator<TransactionalAccount> constructAccountTransactionPaginator(
            API client,
            HandelsbankenSessionStorage sessionStorage);

    protected abstract UpcomingTransactionFetcher<TransactionalAccount> constructUpcomingTransactionFetcher(
            API client,
            HandelsbankenSessionStorage sessionStorage);

    protected abstract TransactionPaginator<CreditCardAccount> constructCreditCardTransactionPaginator(
            API client,
            HandelsbankenSessionStorage sessionStorage);

    @Override
    protected SessionHandler constructSessionHandler() {
        return new HandelsbankenSessionHandler(this.bankClient, this.handelsbankenPersistentStorage,
                this.credentials,
                this.handelsbankenSessionStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new HandelsbankenTransactionalAccountFetcher(this.bankClient,
                                this.handelsbankenSessionStorage),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                constructAccountTransactionPaginator(this.bankClient,
                                        this.handelsbankenSessionStorage),
                                constructUpcomingTransactionFetcher(this.bankClient,
                                        this.handelsbankenSessionStorage)
                        )
                )
        );
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

        return Optional.of(new CreditCardRefreshController(this.metricRefreshController,
                this.updateController,
                constructCreditCardAccountFetcher(
                        this.bankClient, this.handelsbankenSessionStorage),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        constructCreditCardTransactionPaginator(this.bankClient,
                                this.handelsbankenSessionStorage)
                )
        ));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return constructInvestmentRefreshController(this.bankClient,
                this.handelsbankenSessionStorage);
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional
                .of(new LoanRefreshController(this.metricRefreshController, this.updateController,
                        new HandelsbankenLoanFetcher(this.bankClient,
                                this.handelsbankenSessionStorage, this.credentials)
                ));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return constructEInvoiceRefreshController(this.bankClient,
                this.handelsbankenSessionStorage);
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return constructTransferDestinationRefreshController(this.bankClient,
                this.handelsbankenSessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return constructTransferController(this.bankClient, this.handelsbankenSessionStorage,
                this.context);
    }

}
