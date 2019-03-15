package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.Sparebank1Authenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.RestRootResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.FinancialInstitutionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1InvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc.filters.AddRefererFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.sessionhandler.Sparebank1SessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class Sparebank1Agent extends NextGenerationAgent {
    private final Sparebank1ApiClient apiClient;
    private final RestRootResponse restRootResponse;

    public Sparebank1Agent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        String bankId = request.getProvider().getPayload();
        apiClient = new Sparebank1ApiClient(client, bankId);
        FinancialInstitutionEntity financialInstitution = apiClient.getFinancialInstitution();
        restRootResponse = getRestRootResponse(financialInstitution);
    }

    private RestRootResponse getRestRootResponse(FinancialInstitutionEntity financialInstitution) {
        LinkEntity restRootLink = Preconditions.checkNotNull(
                financialInstitution.getLinks().get(Sparebank1Constants.Keys.REST_ROOT_KEY),
                "Link to the rest root not found");

        return apiClient.get(restRootLink.getHref(), RestRootResponse.class);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(Sparebank1Constants.Headers.USER_AGENT);
        AddRefererFilter filter = new AddRefererFilter();
        client.addFilter(filter);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        Sparebank1Authenticator authenticator = new Sparebank1Authenticator(apiClient, credentials,
                persistentStorage, restRootResponse);

        return new AutoAuthenticationController(request, systemUpdater,
                new BankIdAuthenticationControllerNO(context, authenticator), authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController,
                updateController,
                new Sparebank1TransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new Sparebank1TransactionFetcher(apiClient))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController,
                new Sparebank1CreditCardFetcher(apiClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new Sparebank1CreditCardTransactionFetcher(apiClient)))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new Sparebank1InvestmentsFetcher(apiClient)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new Sparebank1LoanFetcher(apiClient)));
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
        return new Sparebank1SessionHandler(apiClient, restRootResponse);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
