package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.HandelsbankenOAuth2Authenticator;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class HandelsbankenUKBaseAgent extends HandelsbankenBaseAgent {

    private static final int MAX_FETCH_PERIOD_MONTHS = 12;
    private HandelsbankenAccountConverter accountConverter;

    public HandelsbankenUKBaseAgent(
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
    protected HandelsbankenBaseAccountConverter getAccountConverter() {
        return accountConverter;
    }

    @Override
    protected LocalDate getMaxPeriodTransactions() {
        return LocalDate.now().minusMonths(MAX_FETCH_PERIOD_MONTHS);
    }

    @Override
    protected String getMarket() {
        return Market.GREAT_BRITAIN;
    }
}
