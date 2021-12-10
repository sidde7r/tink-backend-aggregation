package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.AuthenticationKeys.AUTHENTICATION_FINALISED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.AuthenticationKeys.SMS_SCA;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.client.FabricAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentStatus;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class FabricEmbeddedAuthenticator implements MultiFactorAuthenticator {

    private final PersistentStorage persistentStorage;
    private final FabricAuthApiClient authApiClient;
    private final FabricSupplementalInformationCollector fabricSupplementalInformationCollector;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) {
        ConsentResponse consentResponse = createAndSaveConsent();
        // This bank requires explicit creation of authorisation resource
        AuthorizationResponse authorizationObject =
                authApiClient.createAuthorizationObject(
                        consentResponse.getLinks().getStartAuthorisation());

        authorizationObject =
                authApiClient.updateAuthorizationWithLoginDetails(
                        authorizationObject.getLinks().getUpdatePsuAuthentication(),
                        credentials.getField(Key.USERNAME),
                        credentials.getField(Key.PASSWORD));

        authorizationObject = pickAndSelectScaMethod(authorizationObject);

        throwIfUnexpectedStateAfterSelection(authorizationObject);

        AuthorizationStatusResponse authorizationStatusResponse =
                collectAndSendOtp(authorizationObject);

        validateAndSave(
                authorizationStatusResponse.getScaStatus(),
                consentResponse.getConsentId(),
                credentials);
    }

    private ConsentResponse createAndSaveConsent() {
        ConsentRequest consentRequest =
                ConsentRequest.buildTypicalRecurring(
                        AccessEntity.builder().allPsd2(AccessType.ALL_ACCOUNTS).build(),
                        localDateTimeSource);
        return authApiClient.createConsentForEmbeddedFlow(consentRequest);
    }

    private AuthorizationResponse pickAndSelectScaMethod(
            AuthorizationResponse authorizationObject) {
        if (authorizationObject.isStateScaMethodSelected()) {
            if (isSupportedMethod(authorizationObject.getChosenScaMethod())) {
                return authorizationObject;
            } else {
                throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
            }
        }

        List<ScaMethodEntity> allowedScaMethods =
                authorizationObject.getScaMethods().stream()
                        .filter(this::isSupportedMethod)
                        .collect(Collectors.toList());

        if (allowedScaMethods.isEmpty()) {
            throw LoginError.NO_AVAILABLE_SCA_METHODS.exception();
        }

        if (allowedScaMethods.size() > 1) {
            // As far as we know, these banks only offer possibility of connecting one phone number.
            log.warn(
                    "More than one SMS OTP method found! This is unexpected for this bank. Implementation needs to be improved.");
        }

        ScaMethodEntity selectedMethod = allowedScaMethods.get(0);

        return authApiClient.updateAuthorizationWithMethodId(
                authorizationObject.getLinks().getSelectAuthenticationMethod(),
                selectedMethod.getAuthenticationMethodId());
    }

    private boolean isSupportedMethod(ScaMethodEntity scaMethodEntity) {
        return scaMethodEntity != null
                && SMS_SCA.equalsIgnoreCase(scaMethodEntity.getAuthenticationType());
    }

    private void throwIfUnexpectedStateAfterSelection(AuthorizationResponse authorizationObject) {
        if (!authorizationObject.isStateScaMethodSelected()) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "Unexpected state after login/method selection!");
        }
    }

    private AuthorizationStatusResponse collectAndSendOtp(
            AuthorizationResponse authorizationObject) {
        String smsOtp = fabricSupplementalInformationCollector.collectSmsOtp();
        return authApiClient.updateAuthorizationWithOtpCode(
                authorizationObject.getLinks().getAuthoriseTransaction(), smsOtp);
    }

    private void validateAndSave(String scaStatus, String consentId, Credentials credentials) {
        if (!AUTHENTICATION_FINALISED.equalsIgnoreCase(scaStatus)) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "Invalid sca status after otp authorization: " + scaStatus);
        }

        ConsentStatus consentStatus = authApiClient.getConsentStatus(consentId).getConsentStatus();
        if (!consentStatus.isValid()) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "Invalid consent status after otp authorization: " + consentStatus);
        }

        credentials.setSessionExpiryDate(
                authApiClient.getConsentDetails(consentId).getValidUntil());
        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
    }
}
