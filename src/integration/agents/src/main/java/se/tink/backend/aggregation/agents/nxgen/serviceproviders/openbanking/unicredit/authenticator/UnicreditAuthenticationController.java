package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
public class UnicreditAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final UnicreditAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() {
        authenticator.getConsentId().orElseThrow(SessionError.SESSION_EXPIRED::exception);

        Optional<ConsentDetailsResponse> maybeValidConsentDetails =
                authenticator.getConsentDetailsWithValidStatus();

        if (!maybeValidConsentDetails.isPresent()) {
            authenticator.clearConsent();
            throw SessionError.SESSION_EXPIRED.exception();
        }

        authenticator.setCredentialsSessionExpiryDate(maybeValidConsentDetails.get());
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl =
                this.authenticator.buildAuthorizeUrl(strongAuthenticationState.getState());
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws SessionException, ThirdPartyAppException {

        this.supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);

        Optional<ConsentDetailsResponse> maybeValidConsentDetails =
                authenticator.getConsentDetailsWithValidStatus();

        if (!maybeValidConsentDetails.isPresent()) {
            authenticator.clearConsent();
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
        }

        authenticator.setCredentialsSessionExpiryDate(maybeValidConsentDetails.get());
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
