package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.ConsentProcessor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.UserInteractions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorageProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.ConsentManager;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.PIS_SEPA_RECURRING_PAYMENTS,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER
        })
public final class IccreaAgent extends CbiGlobeAgent {

    @Inject
    public IccreaAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected String getBaseUrl() {
        return "https://psd2.iccreabanca.it";
    }

    @Override
    protected CbiGlobeApiClient getApiClient() {
        return new IccreaApiClient(
                client,
                new CbiStorageProvider(persistentStorage, sessionStorage, temporaryStorage),
                getProviderConfiguration(),
                psuIpAddress,
                randomValueGenerator,
                localDateTimeSource,
                urlProvider);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            ConsentManager consentManager =
                    new ConsentManager(apiClient, userState, localDateTimeSource, urlProvider);

            authenticator =
                    new IccreaAuthenticator(
                            apiClient,
                            strongAuthenticationState,
                            userState,
                            consentManager,
                            getAgentConfiguration().getProviderSpecificConfiguration(),
                            new ConsentProcessor(
                                    consentManager,
                                    new UserInteractions(
                                            supplementalInformationController, catalog),
                                    urlProvider));
        }

        return authenticator;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        CbiGlobePaymentExecutor paymentExecutor =
                new IccreaPaymentExecutor(
                        apiClient,
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState,
                        provider,
                        new UserInteractions(supplementalInformationController, catalog),
                        new ConsentManager(apiClient, userState, localDateTimeSource, urlProvider),
                        urlProvider);
        return Optional.of(
                new PaymentController(
                        paymentExecutor, paymentExecutor, new PaymentControllerExceptionMapper()));
    }
}
