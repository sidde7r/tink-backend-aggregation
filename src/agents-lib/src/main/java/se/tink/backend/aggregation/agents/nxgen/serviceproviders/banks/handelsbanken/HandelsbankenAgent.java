package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.HandelsbankenAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.HandelsbankenCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.creditcard.HandelsbankenCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.loan.HandelsbankenLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
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

public abstract class HandelsbankenAgent<A extends HandelsbankenApiClient, C extends HandelsbankenConfiguration>
        extends NextGenerationAgent {

    private final A bankClient;
    private final HandelsbankenPersistentStorage handelsbankenPersistentStorage;
    private final HandelsbankenSessionStorage handelsbankenSessionStorage;
    private final C handelsbankenConfiguration;

    public HandelsbankenAgent(CredentialsRequest request, AgentContext context,
            C handelsbankenConfiguration) {
        super(request, context);
        this.handelsbankenConfiguration = handelsbankenConfiguration;
        this.handelsbankenPersistentStorage = new HandelsbankenPersistentStorage(this.persistentStorage);
        this.bankClient = constructApiClient(handelsbankenConfiguration);
        this.handelsbankenSessionStorage = new HandelsbankenSessionStorage(this.sessionStorage, handelsbankenConfiguration);

    }

    protected abstract A constructApiClient(C handelsbankenConfiguration);

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setProxy("http://192.168.239.239:8888");
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new TypedAuthenticationController(
                constructAuthenticators(this.bankClient, this.handelsbankenConfiguration,
                        this.handelsbankenPersistentStorage,
                        this.handelsbankenSessionStorage)
        );
    }

    protected abstract TypedAuthenticator[] constructAuthenticators(A bankClient,
            C handelsbankenConfiguration, HandelsbankenPersistentStorage handelsbankenPersistentStorage,
            HandelsbankenSessionStorage handelsbankenSessionStorage);

    protected AutoAuthenticationController constructAutoAuthenticationController(MultiFactorAuthenticator
            cardDeviceAuthenticator) {
        return new AutoAuthenticationController(this.request, this.context,
                cardDeviceAuthenticator,
                new HandelsbankenAutoAuthenticator(this.bankClient, this.handelsbankenPersistentStorage,
                        this.credentials,
                        this.handelsbankenSessionStorage, this.handelsbankenConfiguration)
        );
    }

    protected abstract Optional<InvestmentRefreshController> constructInvestmentRefreshController(A bankClient,
            HandelsbankenSessionStorage handelsbankenSessionStorage);

    protected abstract Optional<EInvoiceRefreshController> constructEInvoiceRefreshController(A client,
            HandelsbankenSessionStorage sessionStorage);

    protected abstract Optional<TransferController> constructTranferController(A client,
            HandelsbankenSessionStorage sessionStorage, AgentContext context);

    protected abstract Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController(A client,
            HandelsbankenSessionStorage sessionStorage);

    @Override
    protected SessionHandler constructSessionHandler() {
        return new HandelsbankenSessionHandler(this.bankClient, this.handelsbankenPersistentStorage, this.credentials,
                this.handelsbankenSessionStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(this.metricRefreshController,
                this.updateController,
                        new HandelsbankenTransactionalAccountFetcher(this.bankClient, this.handelsbankenSessionStorage),
                        new HandelsbankenAccountTransactionFetcher(this.bankClient, this.handelsbankenSessionStorage)
                )
        );
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(new CreditCardRefreshController(this.metricRefreshController, this.updateController,
                new HandelsbankenCreditCardAccountFetcher(this.bankClient, this.handelsbankenSessionStorage),
                new HandelsbankenCreditCardTransactionFetcher(this.bankClient, this.handelsbankenSessionStorage)
        ));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return constructInvestmentRefreshController(this.bankClient, this.handelsbankenSessionStorage);
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(this.metricRefreshController, this.updateController,
                new HandelsbankenLoanFetcher(this.bankClient, this.handelsbankenSessionStorage, this.credentials)
        ));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return constructEInvoiceRefreshController(this.bankClient, this.handelsbankenSessionStorage);
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return constructTransferDestinationRefreshController(this.bankClient, this.handelsbankenSessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return constructTranferController(this.bankClient, this.handelsbankenSessionStorage, this.context);
    }

}
