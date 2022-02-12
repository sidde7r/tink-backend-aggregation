package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkAgentsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.SamlinkCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.SamlinkCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.transactionalaccount.SamlinkTransactionFetcher;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;

public class SamlinkAgent extends BerlinGroupAgent<SamlinkApiClient, SamlinkConfiguration>
        implements RefreshCreditCardAccountsExecutor {

    private final QsealcSigner qsealcSigner;
    private final SamlinkAgentsConfiguration agentConfiguration;
    private final CreditCardRefreshController creditCardRefreshController;
    private final LogMasker logMasker;
    private final SystemUpdater systemUpdater;
    private final LocalDateTimeSource localDateTimeSource;

    public SamlinkAgent(
            AgentComponentProvider componentProvider,
            QsealcSigner qsealcSigner,
            SamlinkAgentsConfiguration agentConfiguration) {
        super(componentProvider);
        this.logMasker = componentProvider.getContext().getLogMasker();
        this.agentConfiguration = agentConfiguration;
        this.qsealcSigner = qsealcSigner;
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.apiClient = createApiClient(componentProvider);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.creditCardRefreshController = getCreditCardRefreshController();
        this.systemUpdater = componentProvider.getSystemUpdater();
    }

    @Override
    protected SamlinkApiClient createApiClient() {
        throw new UnsupportedOperationException("This api client is not used");
    }

    protected SamlinkApiClient createApiClient(AgentComponentProvider componentProvider) {
        return new SamlinkApiClient(
                client,
                persistentStorage,
                qsealcSigner,
                getConfiguration(),
                getConfiguration().getProviderSpecificConfiguration(),
                request,
                agentConfiguration,
                logMasker,
                systemUpdater,
                componentProvider);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new SamlinkAuthenticator(apiClient),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<SamlinkConfiguration> getConfigurationClassDescription() {
        return SamlinkConfiguration.class;
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new SamlinkTransactionFetcher(apiClient, agentConfiguration);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        final SamlinkCardFetcher cardFetcher = new SamlinkCardFetcher(apiClient);
        final SamlinkCardTransactionFetcher transactionFetcher =
                new SamlinkCardTransactionFetcher(
                        apiClient, agentConfiguration, localDateTimeSource);

        return new CreditCardRefreshController(
                metricRefreshController, updateController, cardFetcher, transactionFetcher);
    }
}
