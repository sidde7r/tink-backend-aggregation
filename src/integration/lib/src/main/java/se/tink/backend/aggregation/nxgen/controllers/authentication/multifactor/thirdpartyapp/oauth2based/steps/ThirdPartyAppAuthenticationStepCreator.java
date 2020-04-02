package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessCodeStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.ThirdPartyAppCallbackProcessor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class ThirdPartyAppAuthenticationStepCreator {

    @VisibleForTesting public static final String STEP_NAME = "third_party_app_step";

    private static final long SUPPLEMENTAL_WAIT_TIME_IN_MINUTES = 10L;

    private final ThirdPartyAppCallbackProcessor thirdPartyAppCallbackProcessor;
    private final AccessCodeStorage accessCodeStorage;
    private final StrongAuthenticationState strongAuthenticationState;

    public ThirdPartyAppAuthenticationStep create() {
        return new ThirdPartyAppAuthenticationStep(
                STEP_NAME,
                ThirdPartyAppAuthenticationPayload.of(getAuthorizeUrl()),
                getSupplementalWaitRequest(),
                this::processThirdPartyAppCallback);
    }

    private URL getAuthorizeUrl() {
        return thirdPartyAppCallbackProcessor
                .getOAuth2ThirdPartyAppRequestParamsProvider()
                .getAuthorizeUrl(strongAuthenticationState.getState());
    }

    private SupplementalWaitRequest getSupplementalWaitRequest() {
        return new SupplementalWaitRequest(
                strongAuthenticationState.getSupplementalKey(),
                SUPPLEMENTAL_WAIT_TIME_IN_MINUTES,
                TimeUnit.MINUTES);
    }

    private AuthenticationStepResponse processThirdPartyAppCallback(
            Map<String, String> callbackData) throws AuthorizationException {
        if (!thirdPartyAppCallbackProcessor.isThirdPartyAppLoginSuccessful(callbackData)) {
            throw new AuthorizationException(
                    AuthorizationError.UNAUTHORIZED, "Authorization failed.");
        }

        final String accessCodes =
                thirdPartyAppCallbackProcessor.getAccessCodeFromCallbackData(callbackData);

        accessCodeStorage.storeAccessCodeInSession(accessCodes);

        return AuthenticationStepResponse.executeNextStep();
    }
}
