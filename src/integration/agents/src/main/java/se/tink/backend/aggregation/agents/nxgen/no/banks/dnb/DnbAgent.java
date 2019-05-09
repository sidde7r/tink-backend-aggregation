package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.DnbAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.DnbTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.DnbCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.DnbCreditTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.DnbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.session.DnbSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DnbAgent extends NextGenerationAgent {
    private final DnbApiClient apiClient;
    private final DnbAuthenticator authenticator;
    private final DnbAccountFetcher accountFetcher;
    private final DnbTransactionFetcher transactionFetcher;
    private final DnbCreditCardFetcher creditCardFetcher;
    private final DnbCreditTransactionFetcher creditTransactionFetcher;

    public DnbAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient = new DnbApiClient(client);
        this.authenticator = new DnbAuthenticator(apiClient);
        this.accountFetcher = new DnbAccountFetcher(apiClient);
        this.transactionFetcher = new DnbTransactionFetcher(apiClient);
        this.creditCardFetcher = new DnbCreditCardFetcher(apiClient);
        this.creditTransactionFetcher = new DnbCreditTransactionFetcher(apiClient);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setFollowRedirects(false);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationControllerNO(supplementalRequester, authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                new TransactionPaginationHelper(request), transactionFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        creditTransactionFetcher));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {

        // Disabling investments as our current code doesn't work as expected.

        // DnbInvestmentFetcher investementFetcher = new DnbInvestmentFetcher(apiClient,
        // credentials);
        // return Optional.of(new InvestmentRefreshController(metricRefreshController,
        // updateController, investementFetcher));

        return Optional.empty();
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
        return new DnbSessionHandler();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
