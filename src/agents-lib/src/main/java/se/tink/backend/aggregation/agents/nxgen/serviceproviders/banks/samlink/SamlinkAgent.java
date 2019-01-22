package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.SamlinkAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.authenticator.SamlinkKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.SamlinkCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.SamlinkLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.SamlinkTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.sessionhandler.SamlinkSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticationController;
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
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public abstract class SamlinkAgent extends NextGenerationAgent {

    private final SamlinkApiClient apiClient;
    private final SamlinkPersistentStorage samlinkPersistentStorage;

    public SamlinkAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            SamlinkConfiguration agentConfiguration) {
        super(request, context, signatureKeyPair);
        apiClient = new SamlinkApiClient(client, new SamlinkSessionStorage(sessionStorage), agentConfiguration);
        samlinkPersistentStorage = new SamlinkPersistentStorage(persistentStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(request, context,
                new KeyCardAuthenticationController(catalog, supplementalInformationHelper,
                        new SamlinkKeyCardAuthenticator(apiClient, samlinkPersistentStorage, credentials)),
                new SamlinkAutoAuthenticator(apiClient, samlinkPersistentStorage, credentials));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        SamlinkTransactionalAccountFetcher transactionalAccountFetcher =
                new SamlinkTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(transactionalAccountFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        SamlinkCreditCardFetcher creditCardFetcher = new SamlinkCreditCardFetcher(apiClient);
        return Optional.of(
                new CreditCardRefreshController(metricRefreshController, updateController, creditCardFetcher,
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new SamlinkLoanFetcher(apiClient)));
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
        return new SamlinkSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
