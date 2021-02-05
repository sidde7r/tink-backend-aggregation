package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREATE_BENEFICIARIES;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.CreateBeneficiariesCapabilityExecutor;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.CreditAgricoleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.beneficiary.CreditAgricoleAddBeneficiaryExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.fetcher.transactionalaccounts.CreditAgricoleTransactionalAccountsFetcher;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryExecutor;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREATE_BENEFICIARIES})
public final class CreditAgricoleAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                CreateBeneficiariesCapabilityExecutor {

    private final CreditAgricoleApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private CreditAgricoleAuthenticator authenticator;

    @Inject
    public CreditAgricoleAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = new CreditAgricoleApiClient(client, persistentStorage);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        prepareAuthData();
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

    private void prepareAuthData() {
        if (!persistentStorage.get(StorageKey.IS_DEVICE_REGISTERED, Boolean.class).orElse(false)) {
            storeRegionId();
            storeUserInput();
        }
    }

    private void storeRegionId() {
        String payload = request.getProvider().getPayload();
        if (StringUtils.isBlank(payload)) {
            throw new IllegalStateException("regionId need to be configured in provider payload!");
        }

        persistentStorage.put(StorageKey.REGION_ID, payload);
    }

    private void storeUserInput() {
        persistentStorage.put(
                StorageKey.USER_ACCOUNT_NUMBER, request.getCredentials().getField(Key.USERNAME));
        persistentStorage.put(
                StorageKey.USER_ACCOUNT_CODE, request.getCredentials().getField(Key.PASSWORD));
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

    @Override
    public Optional<CreateBeneficiaryController> constructCreateBeneficiaryController() {
        CreateBeneficiaryExecutor addBeneficiaryExecutor =
                new CreditAgricoleAddBeneficiaryExecutor(
                        apiClient, supplementalInformationHelper, persistentStorage);
        return Optional.of(new CreateBeneficiaryController(addBeneficiaryExecutor));
    }
}
