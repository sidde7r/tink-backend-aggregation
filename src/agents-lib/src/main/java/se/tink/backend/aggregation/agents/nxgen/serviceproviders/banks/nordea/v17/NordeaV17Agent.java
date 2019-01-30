package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.NordeaV17CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment.NordeaV17InvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.loan.NordeaV17LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.NordeaV17TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.parsers.NordeaV17Parser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.session.NordeaV17SessionHandler;
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

public abstract class NordeaV17Agent extends NextGenerationAgent {

    protected final NordeaV17ApiClient nordeaClient;
    protected final NordeaV17Parser parser;

    protected NordeaV17Agent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            NordeaV17Parser parser) {
        super(request, context, signatureKeyPair);

        this.parser = parser;
        this.nordeaClient = constructNordeaClient();
    }

    protected abstract NordeaV17ApiClient constructNordeaClient();

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        NordeaV17TransactionalAccountFetcher transactionalAccountFetcher = new NordeaV17TransactionalAccountFetcher(
                nordeaClient, parser);
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalAccountFetcher),
                        transactionalAccountFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        NordeaV17CreditCardFetcher creditCardFetcher = new NordeaV17CreditCardFetcher(nordeaClient, parser);
        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController, creditCardFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new NordeaV17InvestmentFetcher(nordeaClient, parser)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new NordeaV17LoanFetcher(nordeaClient, parser)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaV17SessionHandler(nordeaClient);
    }
}
