package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREATE_BENEFICIARIES;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.CreateBeneficiariesCapabilityExecutor;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.CaisseEpargneAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.configuration.CaisseEpargneConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.CaisseEpargneCreateBeneficiaryExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.identitydata.CaisseEpargneIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.CaisseEpargneTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.CaisseEpragneTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.storage.CaisseEpargneStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.BpceGroupBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceCookieParserHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceTokenExtractor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.BpceValidationHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.ImageRecognizeHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.module.ImageRecognizerHelperModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;

@AgentDependencyModules(modules = ImageRecognizerHelperModule.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA, CREATE_BENEFICIARIES})
public final class CaisseEpargneAgent extends BpceGroupBaseAgent
        implements CreateBeneficiariesCapabilityExecutor {

    @Getter private final CaisseEpargneApiClient apiClient;

    private final CaisseEpargneStorage caisseEpargneStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CaisseEpargneIdentityDataFetcher caisseEpargneIdentityDataFetcher;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final ImageRecognizeHelper imageRecognizeHelper;
    private final BpceValidationHelper bpceValidationHelper;

    @Inject
    public CaisseEpargneAgent(
            AgentComponentProvider agentComponentProvider,
            ImageRecognizeHelper imageRecognizeHelper) {
        super(agentComponentProvider);

        this.imageRecognizeHelper = imageRecognizeHelper;
        this.caisseEpargneStorage = new CaisseEpargneStorage(this.persistentStorage);
        this.apiClient = createApiClient(agentComponentProvider.getRandomValueGenerator());
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.caisseEpargneIdentityDataFetcher =
                new CaisseEpargneIdentityDataFetcher(this.caisseEpargneStorage);
        this.transferDestinationRefreshController =
                constructTransferDestinationRefreshController(this.apiClient);
        this.bpceValidationHelper = new BpceValidationHelper();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new CaisseEpargneAuthenticator(
                this.apiClient,
                this.caisseEpargneStorage,
                this.supplementalInformationHelper,
                this.imageRecognizeHelper,
                this.bpceValidationHelper);
    }

    @Override
    protected CaisseEpargneApiClient createApiClient(RandomValueGenerator randomValueGenerator) {
        final CaisseEpargneConfiguration caisseEpargneConfiguration =
                new CaisseEpargneConfiguration();
        final BpceTokenExtractor bpceTokenExtractor = new BpceTokenExtractor();
        final BpceCookieParserHelper cookieParserHelper = new BpceCookieParserHelper();

        return new CaisseEpargneApiClient(
                this.client,
                caisseEpargneConfiguration,
                randomValueGenerator,
                this.caisseEpargneStorage,
                bpceTokenExtractor,
                cookieParserHelper);
    }

    @Override
    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        final CaisseEpragneTransactionalAccountFetcher accountFetcher =
                new CaisseEpragneTransactionalAccountFetcher(this.apiClient);
        final CaisseEpargneTransactionalAccountTransactionFetcher transactionFetcher =
                new CaisseEpargneTransactionalAccountTransactionFetcher(getApiClient());

        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        return this.transactionalAccountRefreshController;
    }

    @Override
    protected TransferDestinationRefreshController getTransferDestinationRefreshController() {
        return this.transferDestinationRefreshController;
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                this.caisseEpargneIdentityDataFetcher.fetchIdentityData());
    }

    @Override
    public Optional<CreateBeneficiaryController> constructCreateBeneficiaryController() {
        return Optional.of(
                new CreateBeneficiaryController(
                        new CaisseEpargneCreateBeneficiaryExecutor(
                                this.apiClient,
                                this.supplementalInformationHelper,
                                this.caisseEpargneStorage,
                                this.bpceValidationHelper)));
    }
}
