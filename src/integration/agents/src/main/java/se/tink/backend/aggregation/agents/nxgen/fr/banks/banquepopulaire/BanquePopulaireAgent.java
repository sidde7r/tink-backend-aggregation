package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
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
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public final class BanquePopulaireAgent extends BpceGroupBaseAgent {

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

        storeBankId();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        final BpceValidationHelper validationHelper = new BpceValidationHelper();

        return new BanquePopulaireAuthenticator(
                this.apiClient,
                this.banquePopulaireStorage,
                this.supplementalInformationHelper,
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

    private void storeBankId() {
        final String bankShortId = this.provider.getPayload();
        final String bankId = getBankId(bankShortId);

        this.banquePopulaireStorage.storeBankShortId(bankShortId);
        this.banquePopulaireStorage.storeBankId(bankId);
    }

    private static String getBankId(String bankShortId) {
        if (StringUtils.isBlank(bankShortId) || bankShortId.length() < 3) {
            throw new IllegalArgumentException("Incorrect bank short Id: " + bankShortId);
        }

        return (bankShortId.length() == 3) ? "1" + bankShortId.substring(1) + "07" : bankShortId;
    }
}
