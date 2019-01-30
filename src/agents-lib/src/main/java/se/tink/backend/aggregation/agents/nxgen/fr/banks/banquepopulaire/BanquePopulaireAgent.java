package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.BanquePopulaireAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.creditcard.BanquePopulaireCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.BanquePopulaireLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.BanquePopulaireTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.session.BanquePopulaireSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
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

public class BanquePopulaireAgent extends NextGenerationAgent {
    private BanquePopulaireApiClient apiClient;

    public BanquePopulaireAgent(CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new BanquePopulaireApiClient(client, sessionStorage, request.getProvider().getPayload());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addRedirectHandler(new BanquePopulaireRedirectHandler(sessionStorage));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BanquePopulaireAuthenticator authenticator = new BanquePopulaireAuthenticator(apiClient, sessionStorage);

        return new PasswordAuthenticationController(authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        BanquePopulaireTransactionalAccountsFetcher transactionalAccountFetcher =
                new BanquePopulaireTransactionalAccountsFetcher(apiClient);

        return Optional.of(new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalAccountFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        BanquePopulaireCreditCardFetcher creditCardFetcher = new BanquePopulaireCreditCardFetcher(apiClient);

        CreditCardRefreshController creditCardController = new CreditCardRefreshController(metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher)));

        return Optional.of(creditCardController);
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController,
                updateController,
                new BanquePopulaireLoanFetcher(apiClient)));
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
        return new BanquePopulaireSessionHandler(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
