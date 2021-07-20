package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class StarlingPaymentAuthenticationController {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final OAuth2Authenticator starlingPaymentAuthenticator;

    public StarlingPaymentAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            OAuth2Authenticator starlingPaymentAuthenticator) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
        this.starlingPaymentAuthenticator = starlingPaymentAuthenticator;
    }

    OAuth2Token exchangeToken() throws PaymentException {
        URL url =
                starlingPaymentAuthenticator.buildAuthorizeUrl(
                        strongAuthenticationState.getState());
        openThirdPartyApp(url);
        return starlingPaymentAuthenticator.exchangeAuthorizationCode(
                waitForSupplementalInformation());
    }

    private void openThirdPartyApp(URL redirectUrl) {
        ThirdPartyAppAuthenticationPayload payload = getAppPayload(redirectUrl);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private ThirdPartyAppAuthenticationPayload getAppPayload(URL authorizeUrl) {
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private String waitForSupplementalInformation() throws PaymentException {
        Map<String, String> callback =
                this.supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(),
                                9L,
                                TimeUnit.MINUTES)
                        .orElseThrow(
                                () ->
                                        new PaymentAuthorizationException(
                                                "SCA timeout",
                                                InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT));
        if (callback.containsKey("error")) {
            if ("access_denied".equals(callback.get("error"))) {
                throw new PaymentAuthorizationCancelledByUserException();
            }
            // After more errors are pop up, use switch here
            throw new PaymentAuthorizationException(
                    InternalStatus.PAYMENT_AUTHORIZATION_UNKNOWN_EXCEPTION);
        }

        if (callback.containsKey("code")) {
            return callback.get("code");
        } else {
            throw new PaymentAuthorizationException(InternalStatus.PAYMENT_AUTHORIZATION_FAILED);
        }
    }
}
