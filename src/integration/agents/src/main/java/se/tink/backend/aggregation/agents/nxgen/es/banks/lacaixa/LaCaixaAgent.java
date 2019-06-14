package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.LaCaixaPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.LaCaixaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.identitydata.LaCaixaIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.LaCaixaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.LaCaixaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.LaCaixaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.LaCaixaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.session.LaCaixaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LaCaixaAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {

    private final LaCaixaApiClient apiClient;

    public LaCaixaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new LaCaixaApiClient(client);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new LaCaixaPasswordAuthenticator(apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        LaCaixaAccountFetcher accountFetcher = new LaCaixaAccountFetcher(apiClient);
        LaCaixaTransactionFetcher transactionFetcher = new LaCaixaTransactionFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(transactionFetcher, 0))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        LaCaixaCreditCardFetcher creditCardFetcher = new LaCaixaCreditCardFetcher(apiClient);
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionPagePaginationController<>(creditCardFetcher, 0))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        LaCaixaInvestmentFetcher investmentFetcher = new LaCaixaInvestmentFetcher(apiClient);
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController, updateController, investmentFetcher));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new LaCaixaLoanFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LaCaixaSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new LaCaixaIdentityDataFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
