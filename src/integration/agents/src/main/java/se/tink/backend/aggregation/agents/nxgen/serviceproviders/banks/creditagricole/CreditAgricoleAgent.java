package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.CreditAgricoleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.CreditAgricoleTransactionalAccountsFetcher;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CreditAgricoleAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final CreditAgricoleApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private CreditAgricoleAuthenticator authenticator;

    @Inject
    public CreditAgricoleAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = new CreditAgricoleApiClient(client, persistentStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        storeRegionId();
        storeUserInput();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new CreditAgricoleAuthenticator(
                            apiClient, persistentStorage, supplementalInformationFormer);
        }

        return authenticator;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private void storeRegionId() {
        String payload = request.getProvider().getPayload();
        if (StringUtils.isBlank(payload)) {
            throw new IllegalStateException("regionId need to be configured in provider payload!");
        }

        persistentStorage.put(StorageKey.REGION_ID, payload);
    }

    private void storeUserInput() {
        if (!persistentStorage
                .get(StorageKey.IS_DEVICE_REGISTERED, Boolean.class)
                .orElse(Boolean.FALSE)) {
            persistentStorage.put(
                    StorageKey.USER_ACCOUNT_NUMBER,
                    request.getCredentials().getField(Key.USERNAME));
            persistentStorage.put(
                    StorageKey.USER_ACCOUNT_CODE, request.getCredentials().getField(Key.PASSWORD));
            persistentStorage.put(StorageKey.EMAIL, request.getCredentials().getField(Key.EMAIL));
            persistentStorage.put(
                    StorageKey.PROFILE_PIN, request.getCredentials().getField(Key.ACCESS_PIN));
        }
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        CreditAgricoleTransactionalAccountsFetcher transactionalAccountsFetcher =
                new CreditAgricoleTransactionalAccountsFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalAccountsFetcher,
                transactionalAccountsFetcher);
    }
}
