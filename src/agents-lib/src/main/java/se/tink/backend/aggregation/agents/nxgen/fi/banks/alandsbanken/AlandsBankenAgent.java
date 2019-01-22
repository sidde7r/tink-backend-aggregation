package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.AlandsBankenAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.AlandsBankenMultifFactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.AlandsBankenInvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.AlandsBankenTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.AlandsBankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard.AlandsBankenCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.loan.AlandsBankenLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.messagebodyreaders.CrossKeyMessageBodyReader;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.sessionhandler.AlandsBankenSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class AlandsBankenAgent extends NextGenerationAgent {

    private final AlandsBankenApiClient bankClient;

    public AlandsBankenAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.bankClient = new AlandsBankenApiClient(this.client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addMessageReader(new CrossKeyMessageBodyReader(getClass().getPackage()));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        AlandsBankenPersistentStorage persistentStorage = new AlandsBankenPersistentStorage(this.persistentStorage);
        return new AutoAuthenticationController(this.request, this.context,
                new AlandsBankenMultifFactorAuthenticator(this.bankClient, persistentStorage,
                        this.supplementalInformationHelper),
                new AlandsBankenAutoAuthenticator(this.bankClient, persistentStorage, this.credentials)
        );
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(this.metricRefreshController,
                this.updateController,
                new AlandsBankenTransactionalAccountFetcher(this.bankClient),
                new TransactionFetcherController<>(this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(new AlandsBankenTransactionFetcher(this.bankClient)))
        ));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        AlandsBankenCreditCardFetcher creditCardFetcher = new AlandsBankenCreditCardFetcher(this.bankClient);
        return Optional.of(new CreditCardRefreshController(this.metricRefreshController, this.updateController, creditCardFetcher,
                new TransactionFetcherController<>(this.transactionPaginationHelper,
                        new TransactionDatePaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(this.metricRefreshController, this.updateController,
                new AlandsBankenInvestmentsFetcher(this.bankClient)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(this.metricRefreshController, this.updateController,
                new AlandsBankenLoanFetcher(this.bankClient)));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AlandsBankenSessionHandler(this.bankClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
