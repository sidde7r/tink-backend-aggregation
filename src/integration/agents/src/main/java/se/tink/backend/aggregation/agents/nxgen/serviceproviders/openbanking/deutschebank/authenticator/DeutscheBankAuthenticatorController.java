package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

@Slf4j
public class DeutscheBankAuthenticatorController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private SupplementalInformationHelper supplementalInformationHelper;
    private final DeutscheBankAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;

    public DeutscheBankAuthenticatorController(
            final SupplementalInformationHelper supplementalInformationHelper,
            final DeutscheBankAuthenticator authenticator,
            StrongAuthenticationState strongAuthenticationState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        authenticator.verifyPersistedConsentIdIsNotExpired();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(final String reference) {
        Map<String, String> response =
                supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(),
                                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);
        // ITE-2430: Temporary solution to log what happens in case of failure
        if (!authenticator.isPersistedConsentIdValid()) {
            log.info("Supplemental info response {}", response);
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }
        // authenticator.verifyPersistedConsentIdIsValid()
        authenticator.storeSessionExpiry();
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @SuppressWarnings("Duplicates")
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        final URL authorizeUrl = authenticator.authenticate(strongAuthenticationState.getState());
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(final ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
