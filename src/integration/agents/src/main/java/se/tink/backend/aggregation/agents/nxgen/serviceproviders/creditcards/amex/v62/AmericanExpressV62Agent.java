package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants.Tags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.AmericanExpressV62PasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.UserDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.AmericanExpressV62CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.AmericanExpressV62TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.session.AmericanExpressV62SessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.utils.AmericanExpressV62Storage;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.MultiIpGateway;
import se.tink.backend.aggregation.nxgen.http.filter.ServiceUnavailableBankServiceErrorFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AmericanExpressV62Agent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor, RefreshCreditCardAccountsExecutor {

    private final AmericanExpressV62ApiClient apiClient;
    private final AmericanExpressV62Configuration config;
    private final MultiIpGateway gateway;
    private final AmericanExpressV62Storage instanceStorage;
    private final CreditCardRefreshController creditCardRefreshController;

    protected AmericanExpressV62Agent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            AmericanExpressV62Configuration config) {
        super(request, context, signatureKeyPair);
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());

        this.apiClient =
                new AmericanExpressV62ApiClient(client, sessionStorage, persistentStorage, config);
        this.config = config;
        this.gateway = new MultiIpGateway(client, credentials);
        this.instanceStorage = new AmericanExpressV62Storage();

        this.creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        // Amex is throttling how many requests we can send per IP address.
        // Use this multiIp gateway to originate from different IP addresses.
        gateway.setMultiIpGateway(configuration.getIntegrations());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new AmericanExpressV62PasswordAuthenticator(
                        apiClient, persistentStorage, sessionStorage, instanceStorage));
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
        AmericanExpressV62CreditCardFetcher americanExpressV62CreditCardFetcher =
                AmericanExpressV62CreditCardFetcher.create(config, apiClient, instanceStorage);

        AmericanExpressV62TransactionFetcher americanExpressV62TransactionFetcher =
                AmericanExpressV62TransactionFetcher.create(config, instanceStorage);

        TransactionPagePaginationController<CreditCardAccount>
                amexV66TransactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                americanExpressV62TransactionFetcher,
                                AmericanExpressV62Constants.Fetcher.START_BILLING_INDEX);

        TransactionFetcherController<CreditCardAccount> amexV62TransactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, amexV66TransactionPagePaginationController);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                americanExpressV62CreditCardFetcher,
                amexV62TransactionFetcherController);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new AmericanExpressV62SessionHandler(apiClient, sessionStorage);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return sessionStorage
                .get(Tags.USER_DATA, UserDataEntity.class)
                .map(UserDataEntity::toTinkIdentity)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
