package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.authenticator.SebKortAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.SebKortAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.SebKortTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.fetcher.identitydata.SebKortIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.session.SebKortSessionHandler;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.IdentityData;

public class SebKortAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor, RefreshCreditCardAccountsExecutor {
    private final SebKortApiClient apiClient;
    private final SebKortConfiguration config;
    private final CreditCardRefreshController creditCardRefreshController;

    protected SebKortAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            SebKortConfiguration config) {
        super(request, context, signatureKeyPair);

        this.client.addFilter(new TimeoutFilter());
        this.apiClient = new SebKortApiClient(client, sessionStorage, config);
        this.config = config;

        this.creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new SebKortAuthenticator(apiClient, sessionStorage, config),
                persistentStorage,
                credentials);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new SebKortAccountFetcher(apiClient, config),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new SebKortTransactionFetcher(apiClient))
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SebKortSessionHandler(apiClient, sessionStorage);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final SebKortIdentityDataFetcher fetcher =
                new SebKortIdentityDataFetcher(this.apiClient, credentials);
        final IdentityData identityData = fetcher.fetchIdentityData();
        return new FetchIdentityDataResponse(identityData);
    }
}
