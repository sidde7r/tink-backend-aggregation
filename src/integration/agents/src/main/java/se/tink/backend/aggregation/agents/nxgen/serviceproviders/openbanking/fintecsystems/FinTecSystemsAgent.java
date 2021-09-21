package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.SEPA_CREDIT_TRANSFER;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.FinTechSystemsPaymentExecutor;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentPisCapability(capabilities = SEPA_CREDIT_TRANSFER)
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
    }

    protected FinTecSystemsApiClient constructApiClient() {
        return new FinTecSystemsApiClient(
                providerConfiguration, client, randomValueGenerator, provider);
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
                        providerConfiguration);

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }
}
