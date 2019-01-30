package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.SparebankenVestAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.SparebankenVestOneTimeActivationCodeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.SparebankenVestCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.SparebankenVestCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.SparebankenVestInvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.SparebankenVestLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.SparebankenVestTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.SparebankenVestTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.onetimecode.OneTimeActivationCodeAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class SparebankenVestAgent extends NextGenerationAgent {
    private final SparebankenVestApiClient apiClient;

    public SparebankenVestAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new SparebankenVestApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(SparebankenVestConstants.Headers.USER_AGENT);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        EncapClient encapClient = new EncapClient(new SparebankenVestEncapConfiguration(), persistentStorage,
                client, true, credentials.getField(Field.Key.USERNAME));

        return new AutoAuthenticationController(request, context,
                new OneTimeActivationCodeAuthenticationController(
                        SparebankenVestOneTimeActivationCodeAuthenticator.create(apiClient, encapClient)),
                SparebankenVestAutoAuthenticator.create(apiClient, encapClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        SparebankenVestTransactionFetcher transactionFetcher =
                SparebankenVestTransactionFetcher.create(apiClient, credentials);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                transactionFetcher,
                                SparebankenVestConstants.PagePagination.START_INDEX),
                        transactionFetcher);

        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController,
                        SparebankenVestTransactionalAccountFetcher.create(apiClient, credentials),
                        transactionFetcherController));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

        TransactionFetcher<CreditCardAccount> transactionFetcher =
                new TransactionFetcherController<CreditCardAccount>(transactionPaginationHelper,
                        new TransactionDatePaginationController<CreditCardAccount>(
                                SparebankenVestCreditCardTransactionFetcher.create(apiClient)));

        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController,
                        SparebankenVestCreditCardAccountFetcher.create(apiClient), transactionFetcher));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(metricRefreshController, updateController,
                        SparebankenVestInvestmentsFetcher.create(apiClient)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(metricRefreshController, updateController,
                        SparebankenVestLoanFetcher.create(apiClient)));
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
        return SparebankenVestSessionHandler.create(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
