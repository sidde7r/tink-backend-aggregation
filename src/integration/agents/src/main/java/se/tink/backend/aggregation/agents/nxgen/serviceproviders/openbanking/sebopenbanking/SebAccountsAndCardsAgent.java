package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.LoopProofTransactionFetcherController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.SebCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.SebCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.SebTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SebAccountsAndCardsAgent extends SebAbstractAgent<SebAccountsAndCardsApiClient> {

    public SebAccountsAndCardsAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new SebAccountsAndCardsApiClient(client, sessionStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        SebTransactionalAccountFetcher accountFetcher =
                new SebTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(accountFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new SebCardAccountFetcher(apiClient),
                        // TODO: restore TransactionFetcherController and remove
                        // LoopProofTransactionFetcherController
                        new LoopProofTransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionMonthPaginationController<>(
                                        new SebCardTransactionsFetcher(apiClient),
                                        SebCommonConstants.ZONE_ID))));
    }
}
