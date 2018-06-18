package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.authentication.TargoBankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.TargoBankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.TargoBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.TargoBankCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.TargoBankInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.TargoBankTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.sessionHandler.TargoBankSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
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

public class TargoBankAgent extends NextGenerationAgent {
    private final TargoBankApiClient apiClient;

    protected TargoBankAgent(CredentialsRequest request,
            AgentContext context, TargoBankConfiguration config) {
        super(request, context);
        this.apiClient = new TargoBankApiClient(this.client, config);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                TargoBankPasswordAuthenticator.create(this.apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        TargoBankAccountFetcher.create(this.apiClient, this.sessionStorage),
                        TargoBankTransactionsFetcher.create(this.apiClient, this.sessionStorage)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                TargoBankCreditCardFetcher.create(apiClient, sessionStorage),
                TargoBankCreditCardTransactionsFetcher.create()
        ));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                TargoBankInvestmentAccountFetcher.create(this.apiClient, this.sessionStorage)
        ));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
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
        return TargoBankSessionHandler.create(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
