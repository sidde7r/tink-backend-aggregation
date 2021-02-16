package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.entity.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

public class SibsThirdPartyAppRequestParamsProvider implements ThirdPartyAppRequestParamsProvider {

    private static final long SLEEP_TIME = 10L;
    static final String STEP_ID = "sibsThirdPartyAuthenticationStep";

    private final StrongAuthenticationState strongAuthenticationState;
    private final ConsentManager consentManager;
    private final SibsAuthenticator authenticator;

    SibsThirdPartyAppRequestParamsProvider(
            final ConsentManager consentManager,
            final SibsAuthenticator sibsAuthenticator,
            final StrongAuthenticationState strongAuthenticationState) {
        this.strongAuthenticationState = strongAuthenticationState;
        this.consentManager = consentManager;
        this.authenticator = sibsAuthenticator;
    }

    AuthenticationStepResponse processThirdPartyCallback(Map<String, String> callbackData)
            throws AuthorizationException {
        ConsentStatus consentStatus = consentManager.getStatus();
        if (consentStatus.isAcceptedStatus()) {
            authenticator.handleManualAuthenticationSuccess();
            return AuthenticationStepResponse.executeNextStep();
        } else {
            authenticator.handleManualAuthenticationFailure();
            throw new AuthorizationException(
                    AuthorizationError.UNAUTHORIZED,
                    "Authorization failed, consents status is not accepted.");
        }
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getPayload() {
        return ThirdPartyAppAuthenticationPayload.of(consentManager.create());
    }

    @Override
    public SupplementalWaitRequest getWaitingConfiguration() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(), SLEEP_TIME, TimeUnit.MINUTES);
    }
}
