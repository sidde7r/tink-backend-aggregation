package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.LocalizableKey;

public class RaiffeisenAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final RaiffeisenAuthenticator authenticator;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public RaiffeisenAuthenticationController(
            SupplementalInformationHelper supplementalInformationHelper,
            RaiffeisenAuthenticator authenticator,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            StrongAuthenticationState strongAuthenticationState) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        Optional token = sessionStorage.get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class);
        if (!token.isPresent()) {
            OAuth2Token accessToken = authenticator.fetchToken();
            sessionStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
        }
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {

        sessionStorage.put(RaiffeisenConstants.StorageKeys.OAUTH_TOKEN, authenticator.fetchToken());

        if (Strings.isNullOrEmpty(persistentStorage.get(StorageKeys.CONSENT_ID))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        String consentStatus = authenticator.checkConsentStatus();

        if (!consentStatus.equalsIgnoreCase(RaiffeisenConstants.StatusValues.VALID)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) throws AuthenticationException {

        supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);

        if (authenticator
                .checkConsentStatus()
                .equalsIgnoreCase(RaiffeisenConstants.StatusValues.REJECTED)) {
            throw ThirdPartyAppError.CANCELLED.exception();
        }

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {

        URL authorizeUrl = authenticator.fetchAuthorizeUrl(strongAuthenticationState.getState());

        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
