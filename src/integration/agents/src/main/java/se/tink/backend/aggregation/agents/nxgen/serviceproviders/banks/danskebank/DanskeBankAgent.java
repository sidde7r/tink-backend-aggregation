package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankMultiTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.DanskeBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.DanskeBankInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.filters.DanskeBankHttpFilter;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
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
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public abstract class DanskeBankAgent<MarketSpecificApiClient extends DanskeBankApiClient> extends NextGenerationAgent {
    protected final MarketSpecificApiClient apiClient;
    protected final DanskeBankConfiguration configuration;
    protected final String deviceId;

    public DanskeBankAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            DanskeBankConfiguration configuration) {
        super(request, context, signatureKeyPair);
        this.apiClient = createApiClient(this.client, configuration);
        this.configuration = configuration;
        this.deviceId = Hash.sha1AsHex(this.credentials.getField(Field.Key.USERNAME) + "-TINK");

        // Must add the filter here because `configureHttpClient` is called before the agent constructor
        // (from NextGenerationAgent constructor).
        client.addFilter(new DanskeBankHttpFilter(configuration));
    }

    protected abstract MarketSpecificApiClient createApiClient(TinkHttpClient client,
            DanskeBankConfiguration configuration);

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(this.metricRefreshController,
                this.updateController,
                        new DanskeBankTransactionalAccountFetcher(this.credentials, this.apiClient, this.configuration),
                        creatTransactionFetcherController()));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(new CreditCardRefreshController(this.metricRefreshController, this.updateController,
                new DanskeBankCreditCardFetcher(this.apiClient, this.configuration.getLanguageCode()),
                creatTransactionFetcherController()));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(this.metricRefreshController, this.updateController,
                new DanskeBankInvestmentFetcher(this.apiClient)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(this.metricRefreshController, this.updateController,
                        new DanskeBankLoanFetcher(this.credentials, this.apiClient, this.configuration),
                        creatTransactionFetcherController()));
    }

    private <A extends Account> TransactionFetcherController<A> creatTransactionFetcherController() {
        DanskeBankMultiTransactionsFetcher<A> transactionFetcher = new DanskeBankMultiTransactionsFetcher<>(
                this.apiClient, this.configuration.getLanguageCode());
        return new TransactionFetcherController<>(
                this.transactionPaginationHelper,
                new TransactionDatePaginationController<>(transactionFetcher),
                transactionFetcher
        );
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
        return new DanskeBankSessionHandler(this.apiClient, this.configuration);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
