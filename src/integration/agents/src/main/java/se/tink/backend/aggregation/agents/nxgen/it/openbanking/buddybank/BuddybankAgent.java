package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.BuddybankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.BuddybankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.payment.executor.BuddybankPaymentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.configuration.UnicreditProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.UnicreditPaymentExecutor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class BuddybankAgent extends UnicreditBaseAgent {

    private static final UnicreditProviderConfiguration PROVIDER_CONFIG =
            new UnicreditProviderConfiguration("ALL", "https://api.buddybank.it");

    @Inject
    public BuddybankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, PROVIDER_CONFIG);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BuddybankAuthenticationController(
                new BuddybankAuthenticator((BuddybankApiClient) apiClient),
                strongAuthenticationState,
                supplementalInformationController,
                catalog);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(
                new BuddybankPaymentController(
                        new UnicreditPaymentExecutor(apiClient, sessionStorage),
                        (BuddybankApiClient) apiClient,
                        persistentStorage));
    }
}
