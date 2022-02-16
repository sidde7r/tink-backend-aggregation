package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.IccreaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.UserInteractions;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.payment.IccreaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobePaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.CbiGlobePaymentRequestBuilder;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.payment.exception.PaymentControllerExceptionMapper;
import se.tink.backend.aggregation.nxgen.controllers.payment.validation.impl.SepaCapabilitiesInitializationValidator;
import se.tink.libraries.enums.MarketCode;

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
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new IccreaAuthenticator(
                        authApiClient,
                        fetcherApiClient,
                        storage,
                        localDateTimeSource,
                        credentials,
                        new UserInteractions(supplementalInformationController, catalog),
                        urlProvider),
                new CbiGlobeAutoAuthenticator(authApiClient, storage));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        CbiGlobePaymentExecutor paymentExecutor =
                new IccreaPaymentExecutor(
                        new CbiGlobePaymentApiClient(
                                cbiGlobeHttpClient, urlProvider, providerConfiguration),
                        supplementalInformationController,
                        storage,
                        new CbiGlobePaymentRequestBuilder(),
                        authApiClient,
                        new UserInteractions(supplementalInformationController, catalog),
                        urlProvider,
                        credentials);

        return Optional.of(
                PaymentController.builder()
                        .paymentExecutor(paymentExecutor)
                        .exceptionHandler(new PaymentControllerExceptionMapper())
                        .validator(
                                new SepaCapabilitiesInitializationValidator(
                                        this.getClass(), MarketCode.valueOf(provider.getMarket())))
                        .build());
    }
}
