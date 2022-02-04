package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;
import static se.tink.backend.aggregation.agents.agentcapabilities.PisCapability.SEPA_CREDIT_TRANSFER;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.Constants.REDIRECT_URL;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.authenticator.FinTecSystemsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.FinTecSystemsAccountReportFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.FinTecSystemsReportMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.filters.FTSExceptionFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.FinTechSystemsPaymentExecutor;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;

@AgentCapabilities({TRANSFERS, CHECKING_ACCOUNTS})
@AgentPisCapability(capabilities = SEPA_CREDIT_TRANSFER)
@Slf4j
public class FinTecSystemsAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {
    protected AgentComponentProvider componentProvider;
    protected RandomValueGenerator randomValueGenerator;
    protected LocalDateTimeSource localDateTimeSource;
    private final FinTecSystemsApiClient apiClient;
    private final FinTecSystemsStorage storage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    FinTecSystemsAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        randomValueGenerator = agentComponentProvider.getRandomValueGenerator();
        localDateTimeSource = agentComponentProvider.getLocalDateTimeSource();
        apiClient = constructApiClient();
        client.addFilter(new FTSExceptionFilter());
        storage = new FinTecSystemsStorage(persistentStorage);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private FinTecSystemsApiClient constructApiClient() {
        // One provider of this agent supports multiple blzs.
        String blz =
                provider.getPayload() != null
                        ? provider.getPayload()
                        : credentials.getField("blz-select");

        return new FinTecSystemsApiClient(
                client, randomValueGenerator, readApiKey(), blz, provider.getMarket());
    }

    private String readApiKey() {
        FinTecSystemsConfiguration configuration =
                getAgentConfigurationController()
                        .getAgentConfigurationFromK8s(
                                FinTecSystemsConstants.INTEGRATION_NAME,
                                FinTecSystemsConfiguration.class);
        String apiKey =
                isTestProvider() ? configuration.getTestApiKey() : configuration.getProdApiKey();

        if (StringUtils.isEmpty(apiKey)) {
            throw new IllegalStateException(
                    "apiKey not found in secrets for "
                            + (isTestProvider() ? "test" : "production")
                            + " scenario! Agent cannot work without it!");
        }

        return apiKey;
    }

    private boolean isTestProvider() {
        return provider.getType() == ProviderTypes.TEST;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new FinTecSystemsAuthenticator();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        FinTechSystemsPaymentExecutor paymentExecutor =
                new FinTechSystemsPaymentExecutor(
                        apiClient,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        REDIRECT_URL,
                        storage,
                        this);

        return Optional.of(
                PaymentController.builder()
                        .paymentExecutor(paymentExecutor)
                        .exceptionHandler(new PaymentControllerExceptionMapper())
                        .validator(
                                new SepaCapabilitiesInitializationValidator(
                                        this.getClass(), MarketCode.valueOf(provider.getMarket())))
                        .build());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        FinTecSystemsAccountReportFetcher finTecSystemsAccountReportFetcher =
                new FinTecSystemsAccountReportFetcher(
                        apiClient, storage, new FinTecSystemsReportMapper());
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                finTecSystemsAccountReportFetcher,
                finTecSystemsAccountReportFetcher);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }
}
