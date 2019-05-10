package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.HVBPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.HVBTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.HVBTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.session.HVBSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConfig;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class HVBAgent extends NextGenerationAgent {

    private static final WLConfig wlConfig =
            new WLConfig(
                    HVBConstants.Url.ENDPOINT,
                    HVBPasswordAuthenticator.certificateStringToPublicKey(
                            HVBConstants.SYMMETRIC_CERTIFICATE),
                    HVBConstants.MODULE_NAME,
                    HVBConstants.APP_ID);

    private final WLApiClient apiClient;
    private final HVBStorage storage;

    public HVBAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair keyPair) {
        super(request, context, keyPair);
        apiClient = new WLApiClient(client);
        storage = new HVBStorage(sessionStorage, persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new HVBPasswordAuthenticator(apiClient, storage, wlConfig));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new HVBTransactionalAccountFetcher(apiClient, storage, wlConfig),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        new HVBTransactionFetcher(apiClient, storage, wlConfig)))));
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
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new HVBSessionHandler(apiClient, storage, wlConfig);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
