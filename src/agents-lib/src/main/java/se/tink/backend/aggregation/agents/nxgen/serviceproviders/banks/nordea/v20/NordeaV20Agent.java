package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.NordeaV20CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.NordeaV20InvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.NordeaV20LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.NordeaV20TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.NordeaV20Parser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.session.NordeaV20SessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public abstract class NordeaV20Agent extends NextGenerationAgent {

    protected final NordeaV20ApiClient nordeaClient;
    protected final NordeaV20Parser parser;

    protected NordeaV20Agent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            NordeaV20Parser parser) {
        super(request, context, signatureKeyPair);
        this.parser = parser;
        this.nordeaClient = constructNordeaClient();
    }

    protected abstract NordeaV20ApiClient constructNordeaClient();

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        NordeaV20TransactionalAccountFetcher transactionalAccountFetcher = new NordeaV20TransactionalAccountFetcher(
                nordeaClient, parser);
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalAccountFetcher),
                        transactionalAccountFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        NordeaV20CreditCardFetcher creditCardFetcher = new NordeaV20CreditCardFetcher(nordeaClient, parser);
        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController, creditCardFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new NordeaV20InvestmentFetcher(nordeaClient, parser)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new NordeaV20LoanFetcher(nordeaClient, parser)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaV20SessionHandler(nordeaClient);
    }
}
