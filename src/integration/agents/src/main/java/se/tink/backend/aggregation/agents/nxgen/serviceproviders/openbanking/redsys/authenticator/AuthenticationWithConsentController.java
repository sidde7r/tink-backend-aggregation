package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.TimeUnit;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.pair.Pair;

public class AuthenticationWithConsentController implements MultiFactorAuthenticator {
    final ConsentController consenter;
    final MultiFactorAuthenticator manualAuthenticator;
    final SupplementalInformationHelper supplementalInformationHelper;

    private static final int CONSENT_POLL_ATTEMPTS = 20;
    private static final long CONSENT_SLEEP_SECONDS = TimeUnit.SECONDS.toSeconds(2);

    public AuthenticationWithConsentController(
            OAuth2AuthenticationController oauthController,
            ConsentController consenter,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.consenter = consenter;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.manualAuthenticator =
                new ThirdPartyAppAuthenticationController<>(
                        oauthController, supplementalInformationHelper);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        // first authentication step
        manualAuthenticator.authenticate(credentials);

        // get consent
        if (consenter.storedConsentIsValid()) {
            return;
        }
        getConsent();
    }

    private ThirdPartyAppAuthenticationPayload getRedirectPayload(URL url) {
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(url.get());
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(url.getScheme());
        iOsPayload.setDeepLinkUrl(url.get());
        payload.setIos(iOsPayload);

        return payload;
    }

    private void getConsent() throws AuthorizationException {
        final Pair<String, URL> consentRequest = consenter.requestConsent();
        final String consentId = consentRequest.first;
        final URL consentUrl = consentRequest.second;

        ThirdPartyAppAuthenticationPayload consentPayload = getRedirectPayload(consentUrl);
        supplementalInformationHelper.openThirdPartyApp(consentPayload);

        pollConsent(consentId);
    }

    private void pollConsent(String consentId) throws AuthorizationException {
        for (int i = 0; i < CONSENT_POLL_ATTEMPTS; i++) {
            switch (consenter.getConsentStatus(consentId)) {
                case VALID:
                    // Tell the authenticator which consent ID it can use.
                    consenter.useConsentId(consentId);
                    return;
                case RECEIVED:
                    Uninterruptibles.sleepUninterruptibly(CONSENT_SLEEP_SECONDS, TimeUnit.SECONDS);
                    break;
                default:
                    // Unhandled consent status
                    throw AuthorizationError.UNAUTHORIZED.exception(
                            new LocalizableKey("Did not get consent."));
            }
        }

        throw AuthorizationError.UNAUTHORIZED.exception(new LocalizableKey("Did not get consent."));
    }
}
