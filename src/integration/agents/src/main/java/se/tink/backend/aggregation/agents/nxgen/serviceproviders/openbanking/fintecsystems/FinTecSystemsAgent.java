package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;
import static se.tink.backend.aggregation.agents.agentcapabilities.PisCapability.SEPA_CREDIT_TRANSFER;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.Constants.REDIRECT_URL;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.filters.FTSExceptionFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.FinTechSystemsPaymentExecutor;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;

@AgentCapabilities({TRANSFERS})
@AgentPisCapability(capabilities = SEPA_CREDIT_TRANSFER)
@Slf4j
public class FinTecSystemsAgent extends NextGenerationAgent {
    protected AgentComponentProvider componentProvider;
    protected RandomValueGenerator randomValueGenerator;
    protected LocalDateTimeSource localDateTimeSource;
    private FinTecSystemsConfiguration providerConfiguration;
    private FinTecSystemsApiClient apiClient;

    @Inject
    FinTecSystemsAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        randomValueGenerator = agentComponentProvider.getRandomValueGenerator();
        localDateTimeSource = agentComponentProvider.getLocalDateTimeSource();
        providerConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(FinTecSystemsConfiguration.class)
                        .getProviderSpecificConfiguration();
        apiClient = constructApiClient();
        client.addFilter(new FTSExceptionFilter());
    }

    private FinTecSystemsApiClient constructApiClient() {
        String blz;
        if (provider.getPayload() != null) {
            blz = provider.getPayload();
        } else {
            // One provider of this agent supports multiple blzs.
            blz = credentials.getField("blz-select");
        }

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
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return null;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {

        FinTechSystemsPaymentExecutor paymentExecutor =
                new FinTechSystemsPaymentExecutor(
                        apiClient,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        providerConfiguration,
                        REDIRECT_URL); // after decision over FTS provider should have accessType OB
        // or some thing else. Later Update code accordingly. Currently redirect URL is auto
        // populated only  when provider is OB for  access type like OTHER auto population of
        // redirect URL dont not work.

        return Optional.of(
                new PaymentController(
                        paymentExecutor, paymentExecutor, new PaymentControllerExceptionMapper()));
    }
}
