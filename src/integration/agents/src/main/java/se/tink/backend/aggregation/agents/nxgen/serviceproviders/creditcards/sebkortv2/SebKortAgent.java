package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.SebKortAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.SEBKortAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.SEBKortTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.session.SebKortSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebKortAgent extends NextGenerationAgent {
    private final SebKortApiClient apiClient;
    private final SebKortConfiguration config;

    protected SebKortAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            SebKortConfiguration config) {
        super(request, context, signatureKeyPair);

        this.apiClient = new SebKortApiClient(client, sessionStorage, config);
        this.config = config;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester, new SebKortAuthenticator(apiClient, sessionStorage, config));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new SEBKortAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new SEBKortTransactionFetcher(apiClient)))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SebKortSessionHandler(apiClient, sessionStorage);
    }
}
