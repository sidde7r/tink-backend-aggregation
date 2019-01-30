package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.BunqRegistrationAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.BunqTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.BunqTransactionalTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.filter.BunqRequiredHeadersFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.filter.BunqSignatureHeaderFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
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
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class BunqAgent extends NextGenerationAgent {
    private final BunqApiClient apiClient;

    public BunqAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        String backendHost = Preconditions.checkNotNull(request.getProvider().getPayload());
        BunqConfiguration agentConfiguration = new BunqConfiguration(backendHost);
        this.apiClient = new BunqApiClient(client, agentConfiguration);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BunqRequiredHeadersFilter(sessionStorage));
        client.addFilter(new BunqSignatureHeaderFilter(persistentStorage, client.getUserAgent()));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BunqAuthenticator(request,
                new BunqRegistrationAuthenticator(persistentStorage, sessionStorage, apiClient,
                        getAggregatorInfo().getAggregatorIdentifier()),
                new BunqAutoAuthenticator(credentials, persistentStorage, sessionStorage, apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                new BunqTransactionalAccountFetcher(sessionStorage, apiClient),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new BunqTransactionalTransactionsFetcher(sessionStorage, apiClient)))));
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
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BunqSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
