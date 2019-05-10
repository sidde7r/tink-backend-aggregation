package se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.authenticator.BankAustriaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.fetcher.BankAustriaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml.OtmlBodyReader;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.otml.OtmlResponseConverter;
import se.tink.backend.aggregation.agents.nxgen.at.banks.bankaustria.sessionhandler.BankAustriaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BankAustriaAgent extends NextGenerationAgent {

    private final OtmlResponseConverter otmlResponseConverter;
    private final BankAustriaSessionStorage bankAustriaSessionStorage;
    private BankAustriaApiClient apiClient;

    public BankAustriaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.bankAustriaSessionStorage =
                new BankAustriaSessionStorage(
                        this.sessionStorage,
                        BankAustriaConstants.Device.IPHONE7_OTML_LAYOUT_INITIAL);
        this.apiClient = new BankAustriaApiClient(this.client, bankAustriaSessionStorage);
        this.otmlResponseConverter = new OtmlResponseConverter();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addMessageReader(new OtmlBodyReader<>());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BankAustriaAuthenticator authenticator =
                new BankAustriaAuthenticator(
                        this.apiClient,
                        this.credentials,
                        this.persistentStorage,
                        bankAustriaSessionStorage,
                        otmlResponseConverter);

        return new PasswordAuthenticationController(authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        BankAustriaTransactionalAccountFetcher transactionalAccountFetcher =
                new BankAustriaTransactionalAccountFetcher(apiClient, otmlResponseConverter);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        transactionalAccountFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BankAustriaSessionHandler(apiClient, otmlResponseConverter);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
