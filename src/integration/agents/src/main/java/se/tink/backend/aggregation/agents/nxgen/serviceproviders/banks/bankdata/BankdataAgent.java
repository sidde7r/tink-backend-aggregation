package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.BankdataPinAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.sessionhandler.BankdataSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BankdataAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor {
    private BankdataApiClient bankClient;
    private final InvestmentRefreshController investmentRefreshController;

    public BankdataAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);

        bankClient = new BankdataApiClient(client, request.getProvider());

        BankdataInvestmentFetcher investmentFetcher = new BankdataInvestmentFetcher(bankClient);
        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController, updateController, investmentFetcher);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setDebugOutput(false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new BankdataPinAuthenticator(bankClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        BankdataTransactionFetcher transactionFetcher = new BankdataTransactionFetcher(bankClient);
        BankdataTransactionalAccountFetcher accountFetcher =
                new BankdataTransactionalAccountFetcher(bankClient);

        TransactionPagePaginationController<TransactionalAccount>
                transactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                transactionFetcher, BankdataConstants.Fetcher.START_PAGE);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        transactionPagePaginationController,
                        transactionFetcher);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        transactionFetcherController));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        BankdataCreditCardAccountFetcher ccAccountFetcher =
                new BankdataCreditCardAccountFetcher(bankClient);
        BankdataCreditCardTransactionFetcher ccTransactionFetcher =
                new BankdataCreditCardTransactionFetcher(bankClient);

        TransactionPagePaginationController<CreditCardAccount>
                ccTransactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                ccTransactionFetcher, BankdataConstants.Fetcher.START_PAGE);

        TransactionFetcherController<CreditCardAccount> ccTransactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, ccTransactionPagePaginationController);

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        ccAccountFetcher,
                        ccTransactionFetcherController));
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
        return new BankdataSessionHandler();
    }
}
