package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
@RequiredArgsConstructor
public class FabricRedirectAuthenticator implements ThirdPartyAppAuthenticator<String> {

    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final FabricAuthApiClient authApiClient;
    private final Credentials credentials;
    private final LocalDateTimeSource localDateTimeSource;
    private final String customersBaseRedirectUrl;

    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl = buildAuthorizeUrl();
        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    private URL buildAuthorizeUrl() {
        ConsentRequest consentRequest =
                ConsentRequest.buildTypicalRecurring(
                        AccessEntity.builder().allPsd2(AccessType.ALL_ACCOUNTS).build(),
                        localDateTimeSource);
        ConsentResponse consentResponse =
                authApiClient.createConsent(buildCustomersRedirectUrl(), consentRequest);
        persistentStorage.put(StorageKeys.CONSENT_ID, consentResponse.getConsentId());
        return new URL(consentResponse.getLinks().getScaRedirect());
    }

    private URL buildCustomersRedirectUrl() {
        return new URL(customersBaseRedirectUrl)
                .queryParam(QueryKeys.STATE, strongAuthenticationState.getState());
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        Optional<Map<String, String>> maybeCallbackData =
                supplementalInformationHelper.waitForSupplementalInformation(
                        strongAuthenticationState.getSupplementalKey(),
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES);
        if (!maybeCallbackData.isPresent()) {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.TIMED_OUT);
        }
        handleErrors(maybeCallbackData.get());

        Optional<String> maybeConsentId =
                persistentStorage.get(FabricConstants.StorageKeys.CONSENT_ID, String.class);
        if (maybeConsentId.isPresent()) {
            setSessionExpiryDateBasedOnConsent(maybeConsentId.get());
        } else {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
        }
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private void handleErrors(Map<String, String> callbackData) {
        Optional<String> error = getCallbackElement(callbackData, "error");
        Optional<String> errorDescription = getCallbackElement(callbackData, "error_description");
        if (!error.isPresent()) {
            log.info("Callback success.");
        } else {
            OAuth2Constants.ErrorType errorType =
                    OAuth2Constants.ErrorType.getErrorType(error.get());
            if (!OAuth2Constants.ErrorType.ACCESS_DENIED.equals(errorType)
                    && !OAuth2Constants.ErrorType.LOGIN_REQUIRED.equals(errorType)) {
                throw new IllegalStateException(
                        "Unknown error: " + errorType + ":" + errorDescription.orElse("") + ".");
            } else {
                log.info(
                        "{} callback: {}",
                        errorType.getValue(),
                        SerializationUtils.serializeToString(callbackData));
                throw LoginError.INCORRECT_CREDENTIALS.exception();
            }
        }
    }

    private Optional<String> getCallbackElement(Map<String, String> callbackData, String key) {
        return Optional.ofNullable(callbackData.get(key)).filter(x -> !x.isEmpty());
    }

    private void setSessionExpiryDateBasedOnConsent(String consentId) {
        ConsentDetailsResponse consentDetails = authApiClient.getConsentDetails(consentId);
        credentials.setSessionExpiryDate(consentDetails.getValidUntil());
    }
}
