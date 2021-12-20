package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.ConsentController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.enums.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RedsysAuthenticationController extends OAuth2AuthenticationController {
    private static final Logger LOG = LoggerFactory.getLogger(RedsysAuthenticationController.class);
    private final ConsentController consentController;
    private final PersistentStorage persistentStorage;
    private final OAuth2Authenticator oAuth2Authenticator;

    public RedsysAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            ConsentController consentController,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {
        super(
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                credentials,
                strongAuthenticationState);
        this.oAuth2Authenticator = authenticator;
        this.consentController = consentController;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {
        // Perform oAuth2 authentication
        final ThirdPartyAppResponse<String> oauthResponse = super.collect(reference);

        // Redsys token is valid only for 500 sec and it's needed even for fetching. So just in case
        // additional refresh token is requested after authentication to get it valid for entire
        // fetching phase
        refreshToken();

        // Request consent
        if (!consentController.requestConsent()) {
            LOG.info("Did not get consent");
            removeStoredTokens();
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
        }

        return oauthResponse;
    }

    private void refreshToken() {
        String refreshToken = getPreviousRefreshTokenFromStorage();
        OAuth2Token newToken = oAuth2Authenticator.refreshAccessToken(refreshToken);
        if (!newToken.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, newToken);
        oAuth2Authenticator.useAccessToken(newToken);
    }

    private String getPreviousRefreshTokenFromStorage() {
        return persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception)
                .getRefreshToken()
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        super.autoAuthenticate();

        // Check consent
        final String consentId = consentController.getConsentId();
        if (Strings.isNullOrEmpty(consentId)
                || !consentController.fetchConsentStatus(consentId).equals(ConsentStatus.VALID)) {
            LOG.info("Consent empty or invalid, expiring session");
            removeStoredTokens();
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private void removeStoredTokens() {
        persistentStorage.remove(PersistentStorageKeys.OAUTH_2_TOKEN);
        consentController.clearConsentStorage();
    }
}
