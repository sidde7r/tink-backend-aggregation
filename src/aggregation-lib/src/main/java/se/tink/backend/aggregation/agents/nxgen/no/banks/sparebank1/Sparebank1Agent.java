package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import com.google.common.base.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.Sparebank1Authenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.authentication.RestRootResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.FinancialInstitutionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1InvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc.FinancialInstituationsListResponse;
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
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Urls;

public class Sparebank1Agent extends NextGenerationAgent {
    private final Sparebank1Authenticator authenticator;
    private final Sparebank1TransactionFetcher transactionFetcher;
    private final Sparebank1CreditCardTransactionFetcher creditCardTransactionFetcher;
    private final Sparebank1ApiClient apiClient;
    private final String bankKey;
    private final RestRootResponse restRootResponse;

    public Sparebank1Agent(CredentialsRequest request, AgentContext context) {
        super(request, context);

        this.apiClient = new Sparebank1ApiClient(this.client);
        this.bankKey = request.getProvider().getPayload();
        FinancialInstitutionEntity financialInstitution = getFinancialInstitution();
        this.restRootResponse = getRestRootResponse(financialInstitution);

        this.authenticator = new Sparebank1Authenticator(this.apiClient, this.credentials, this.persistentStorage,
                this.bankKey,
                financialInstitution, this.restRootResponse);
        this.transactionFetcher = new Sparebank1TransactionFetcher(this.apiClient, this.sessionStorage);
        this.creditCardTransactionFetcher = new Sparebank1CreditCardTransactionFetcher(this.apiClient, this.bankKey);
    }

    private FinancialInstitutionEntity getFinancialInstitution() {
        Optional<FinancialInstitutionEntity> finInstitution = this.apiClient
                .get(Urls.CMS, FinancialInstituationsListResponse.class)
                .getFinancialInstitutions()
                .stream()
                .filter(fe -> Objects.equal(fe.getId(), this.bankKey))
                .findFirst();

        if (!finInstitution.isPresent()) {
            throw new IllegalStateException(String.format("Bank (%s) not present in list of banks", this.bankKey));
        }

        return finInstitution.get();
    }

    private RestRootResponse getRestRootResponse(FinancialInstitutionEntity financialInstitution) {
        return this.apiClient
                .get(financialInstitution.getLinks().get(Sparebank1Constants.REST_ROOT_KEY).getHref(), RestRootResponse.class);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        AddRefererFilter filter = new AddRefererFilter();
        client.addFilter(filter);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(this.request, this.context,
                new BankIdAuthenticationControllerNO(this.context, this.authenticator), this.authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(this.metricRefreshController,
                this.updateController,
                new Sparebank1TransactionalAccountFetcher(this.apiClient, this.bankKey, this.sessionStorage),
                new TransactionFetcherController<>(this.transactionPaginationHelper, this.transactionFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(new CreditCardRefreshController(this.metricRefreshController, this.updateController,
                new Sparebank1CreditCardFetcher(this.apiClient, this.bankKey),
                new TransactionFetcherController<>(this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(this.creditCardTransactionFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(this.metricRefreshController, this.updateController,
                new Sparebank1InvestmentsFetcher(this.apiClient, this.credentials, this.bankKey)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(this.metricRefreshController, this.updateController,
                new Sparebank1LoanFetcher(this.apiClient, this.bankKey)));
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
        return new Sparebank1SessionHandler(this.apiClient, this.bankKey, this.restRootResponse);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
