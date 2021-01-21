package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.time.LocalDate;
import java.util.Optional;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.executor.payment.HandelsbankenPaymentExecutorSelector;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenOAuth2Authenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class HandelsbankenAgent extends HandelsbankenBaseAgent {

    private static final int MAX_FETCH_PERIOD_MONTHS = 12;
    private final HandelsbankenAccountConverter accountConverter;

    public HandelsbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        accountConverter = new HandelsbankenAccountConverter();
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new HandelsbankenOAuth2Authenticator(
                                apiClient, agentConfiguration, persistentStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected String getMarket() {
        return Market.FINLAND;
    }

    @Override
    protected LocalDate getMaxPeriodTransactions() {
        return LocalDate.now().minusMonths(MAX_FETCH_PERIOD_MONTHS);
    }

    @Override
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        HandelsbankenPaymentExecutorSelector paymentExecutorSelector =
                new HandelsbankenPaymentExecutorSelector(apiClient);
        return Optional.of(new PaymentController(paymentExecutorSelector, paymentExecutorSelector));
    }
}
