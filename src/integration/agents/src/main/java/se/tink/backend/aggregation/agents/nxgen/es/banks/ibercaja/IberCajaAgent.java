package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.authenticator.IberCajaPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.identitydata.IberCajaIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaCreditCardTransactionalFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaTransactionalFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.session.IberCajaSessionHandler;
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
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IberCajaAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {

    private final IberCajaApiClient apiClient;
    private final IberCajaSessionStorage iberCajaSessionStorage;

    public IberCajaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.iberCajaSessionStorage = new IberCajaSessionStorage(sessionStorage);
        this.apiClient = new IberCajaApiClient(client, iberCajaSessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new IberCajaPasswordAuthenticator(apiClient, iberCajaSessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        IberCajaAccountFetcher accountFetcher = new IberCajaAccountFetcher(apiClient);
        IberCajaTransactionalFetcher transactionalFetcher =
                new IberCajaTransactionalFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(transactionalFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        IberCajaCreditCardFetcher creditCardFetcher = new IberCajaCreditCardFetcher(apiClient);
        IberCajaCreditCardTransactionalFetcher transactionalFetcher =
                new IberCajaCreditCardTransactionalFetcher(apiClient);

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(transactionalFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {

        IberCajaInvestmentAccountFetcher investmentAccountFetcher =
                new IberCajaInvestmentAccountFetcher(apiClient);
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController, updateController, investmentAccountFetcher));
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
        return new IberCajaSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IberCajaIdentityDataFetcher fetcher =
                new IberCajaIdentityDataFetcher(iberCajaSessionStorage);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
