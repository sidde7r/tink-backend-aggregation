package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.Values;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@RequiredArgsConstructor
@Slf4j
public class CitadeleThirdPartyAppRequestParamsProvider
        implements ThirdPartyAppRequestParamsProvider {

    private final CitadeleConsentManager citadeleConsentManager;
    private final StrongAuthenticationState strongAuthenticationState;
    private final Credentials credentials;

    @Override
    public ThirdPartyAppAuthenticationPayload getPayload() {
        return ThirdPartyAppAuthenticationPayload.of(citadeleConsentManager.getConsentRequest());
    }

    @Override
    public SupplementalWaitRequest getWaitingConfiguration() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);
    }

    AuthenticationStepResponse processThirdPartyCallback(Map<String, String> callbackData) {

        if (callbackData.get(QueryKeys.OK).equals(String.valueOf(false))) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception(
                    "Authorization process cancelled or bad credentials provided.");
        } else {
            credentials.setSessionExpiryDate(LocalDateTime.now().plusDays(Values.HISTORY_MAX_DAYS));
            return AuthenticationStepResponse.authenticationSucceeded();
        }
    }
}
