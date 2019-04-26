package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.AmericanExpressV62PasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.AmericanExpressV62CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.AmericanExpressV62TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.session.AmericanExpressV62SessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.MultiIpGateway;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AmericanExpressV62Agent extends NextGenerationAgent {

    private final AmericanExpressV62ApiClient apiClient;
    private final AmericanExpressV62Configuration config;
    private final MultiIpGateway gateway;

    protected AmericanExpressV62Agent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            AmericanExpressV62Configuration config) {
        super(request, context, signatureKeyPair);
        this.apiClient =
                new AmericanExpressV62ApiClient(client, sessionStorage, persistentStorage, config);
        this.config = config;
        this.gateway = new MultiIpGateway(client, credentials);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        // Amex is throttling how many requests we can send per IP address.
        // Use this multiIp gateway to originate from different IP addresses.
        gateway.setMultiIpGateway(configuration.getIntegrations());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new AmericanExpressV62PasswordAuthenticator(
                        apiClient, persistentStorage, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        AmericanExpressV62CreditCardFetcher americanExpressV62CreditCardFetcher =
                AmericanExpressV62CreditCardFetcher.create(sessionStorage, config);

        AmericanExpressV62TransactionFetcher americanExpressV62TransactionFetcher =
                AmericanExpressV62TransactionFetcher.create(apiClient, config, sessionStorage);

        TransactionPagePaginationController<CreditCardAccount>
                amexV66TransactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                americanExpressV62TransactionFetcher,
                                AmericanExpressV62Constants.Fetcher.START_PAGE);

        TransactionFetcherController<CreditCardAccount> amexV62TransactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, amexV66TransactionPagePaginationController);

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        americanExpressV62CreditCardFetcher,
                        amexV62TransactionFetcherController));
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
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AmericanExpressV62SessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
