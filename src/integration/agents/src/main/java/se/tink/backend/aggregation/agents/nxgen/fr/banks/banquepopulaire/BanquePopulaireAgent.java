package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.util.List;
import lombok.Getter;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.BanquePopulaireAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.configuration.BanquePopulaireConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.converter.BanquePopulaireConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.account.BanquePopulaireAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.identity.BanquePopulaireIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transaction.BanquePopulaireTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.storage.BanquePopulaireStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.BpceGroupBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceCookieParserHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceTokenExtractor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.BpceValidationHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.ImageRecognizeHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.module.ImageRecognizerHelperModule;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;

@AgentDependencyModules(modules = ImageRecognizerHelperModule.class)
public class BanquePopulaireAgent extends BpceGroupBaseAgent {

    @Getter private final BanquePopulaireApiClient apiClient;
    private final BanquePopulaireConverter banquePopulaireConverter;
    private final BanquePopulaireStorage banquePopulaireStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BanquePopulaireIdentityFetcher banquePopulaireIdentityFetcher;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final ImageRecognizeHelper imageRecognizeHelper;

    @Inject
    public BanquePopulaireAgent(
            AgentComponentProvider agentComponentProvider,
            ImageRecognizeHelper imageRecognizeHelper) {
        super(agentComponentProvider);

        this.imageRecognizeHelper = imageRecognizeHelper;
        this.banquePopulaireStorage = new BanquePopulaireStorage(this.persistentStorage);
        this.banquePopulaireConverter = new BanquePopulaireConverter(new ObjectMapper());
        this.apiClient = createApiClient(agentComponentProvider.getRandomValueGenerator());
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.banquePopulaireIdentityFetcher =
                new BanquePopulaireIdentityFetcher(this.banquePopulaireStorage);
        this.transferDestinationRefreshController =
                constructTransferDestinationRefreshController(this.apiClient);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        final BpceValidationHelper validationHelper = new BpceValidationHelper();

        return new BanquePopulaireAuthenticator(
                this.apiClient,
                this.banquePopulaireStorage,
                this.supplementalInformationProvider,
                this.imageRecognizeHelper,
                validationHelper);
    }

    @Override
    protected BanquePopulaireApiClient createApiClient(RandomValueGenerator randomValueGenerator) {
        final BanquePopulaireConfiguration banquePopulaireConfiguration =
                new BanquePopulaireConfiguration();
        final BpceTokenExtractor bpceTokenExtractor = new BpceTokenExtractor();
        final BpceCookieParserHelper cookieParserHelper = new BpceCookieParserHelper();

        return new BanquePopulaireApiClient(
                this.client,
                banquePopulaireConfiguration,
                randomValueGenerator,
                this.banquePopulaireStorage,
                bpceTokenExtractor,
                this.banquePopulaireConverter,
                cookieParserHelper);
    }

    @Override
    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        final BanquePopulaireAccountsFetcher accountFetcher =
                new BanquePopulaireAccountsFetcher(this.apiClient, this.banquePopulaireConverter);
        final BanquePopulaireTransactionFetcher transactionFetcher =
                new BanquePopulaireTransactionFetcher(getApiClient());

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
                this.banquePopulaireIdentityFetcher.fetchIdentityData());
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return this.transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
