package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.Consent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Android;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Desktop;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload.Ios;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FabricRedirectAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(FabricRedirectAuthenticationController.class);
    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final FabricAuthenticator authenticator;
    private final String strongAuthenticationState;
    private final String strongAuthenticationStateSupplementalKey;

    public FabricRedirectAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            FabricAuthenticator authenticator,
            StrongAuthenticationState strongAuthenticationState) {
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.strongAuthenticationStateSupplementalKey =
                strongAuthenticationState.getSupplementalKey();
        this.strongAuthenticationState = strongAuthenticationState.getState();
    }

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @SuppressWarnings("Duplicates")
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl = authenticator.buildAuthorizeUrl(strongAuthenticationState);
        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();
        Android androidPayload = new Android();
        androidPayload.setIntent(authorizeUrl.get());
        payload.setAndroid(androidPayload);
        Ios iOsPayload = new Ios();
        iOsPayload.setAppScheme(authorizeUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());
        payload.setIos(iOsPayload);
        Desktop desktop = new Desktop();
        desktop.setUrl(authorizeUrl.get());
        payload.setDesktop(desktop);
        return payload;
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) throws AuthenticationException {
        Optional<Map<String, String>> maybeCallbackData =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationStateSupplementalKey,
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);
        if (!maybeCallbackData.isPresent()) {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.TIMED_OUT);
        }
        handleErrors(maybeCallbackData.get());

        Optional<String> maybeConsentId =
                persistentStorage.get(FabricConstants.StorageKeys.CONSENT_ID, String.class);
        if (maybeConsentId.isPresent()) {
            authenticator.setSessionExpiryDateBasedOnConsent(maybeConsentId.get());
        } else {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
        }
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private Optional<String> getCallbackElement(Map<String, String> callbackData, String key) {
        return Optional.ofNullable(callbackData.get(key)).filter(x -> !x.isEmpty());
    }

    private void handleErrors(Map<String, String> callbackData) throws AuthenticationException {
        Optional<String> error = getCallbackElement(callbackData, "error");
        Optional<String> errorDescription = getCallbackElement(callbackData, "error_description");
        if (!error.isPresent()) {
            logger.info("Callback success.");
        } else {
            OAuth2Constants.ErrorType errorType =
                    OAuth2Constants.ErrorType.getErrorType(error.get());
            if (!OAuth2Constants.ErrorType.ACCESS_DENIED.equals(errorType)
                    && !OAuth2Constants.ErrorType.LOGIN_REQUIRED.equals(errorType)) {
                throw new IllegalStateException(
                        "Unknown error: " + errorType + ":" + errorDescription.orElse("") + ".");
            } else {
                logger.info(
                        "{} callback: {}",
                        errorType.getValue(),
                        SerializationUtils.serializeToString(callbackData));
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        Optional<String> consentId =
                persistentStorage.get(FabricConstants.StorageKeys.CONSENT_ID, String.class);
        if (!consentId.isPresent()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        ConsentStatusResponse consentStatusResponse =
                authenticator.getConsentStatus(consentId.get());
        if (!consentStatusResponse.getConsentStatus().equalsIgnoreCase(Consent.VALID)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
