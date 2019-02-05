package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.NordeaV21CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.NordeaV21InvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.loan.NordeaV21LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.NordeaV21TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.parsers.NordeaV21Parser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.session.NordeaV21SessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public abstract class NordeaV21Agent extends NextGenerationAgent {

    protected final NordeaV21ApiClient nordeaClient;
    protected final NordeaV21Parser parser;

    protected NordeaV21Agent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair,
            NordeaV21Parser parser) {
        super(request, context, signatureKeyPair);

        this.parser = parser;
        this.nordeaClient = constructNordeaClient();
    }

    protected abstract NordeaV21ApiClient constructNordeaClient();

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        NordeaV21TransactionalAccountFetcher transactionalAccountFetcher = new NordeaV21TransactionalAccountFetcher(
                nordeaClient, parser);
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalAccountFetcher),
                        transactionalAccountFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        NordeaV21CreditCardFetcher creditCardFetcher = new NordeaV21CreditCardFetcher(nordeaClient, parser);
        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController, creditCardFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new NordeaV21InvestmentFetcher(nordeaClient, parser)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new NordeaV21LoanFetcher(nordeaClient, parser)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaV21SessionHandler(nordeaClient);
    }
}
