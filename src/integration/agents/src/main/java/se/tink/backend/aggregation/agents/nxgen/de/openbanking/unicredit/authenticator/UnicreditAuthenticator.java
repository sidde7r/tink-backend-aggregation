package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.UnicreditApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.detail.UnicreditEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.authenticator.rpc.UnicreditConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ChallengeDataEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

@Slf4j
@RequiredArgsConstructor
public class UnicreditAuthenticator
        implements MultiFactorAuthenticator, AutoAuthenticator, PaymentAuthenticator {

    private final UnicreditApiClient apiClient;
    private final UnicreditStorage storage;
    private final Credentials credentials;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationController supplementalInformationController;
    private final UnicreditEmbeddedFieldBuilder embeddedFieldBuilder;

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate() {
        String consentId =
                storage.getConsentId().orElseThrow(SessionError.SESSION_EXPIRED::exception);
        verifyConsentValidity(consentId);
    }

    @Override
    public void authenticate(Credentials credentials) {
        UnicreditConsentResponse consent = createAndSaveConsent();

        AuthorizationResponse initializeAuthorizationResponse =
                initializeAuthorization(consent.getLinks().getStartAuthorisation(), credentials);

        AuthorizationResponse authorizationResponse =
                authorizeWithPassword(
                        initializeAuthorizationResponse.getLinks().getUpdatePsuAuthentication(),
                        credentials);

        authorizeWithOtp(authorizationResponse);
        verifyConsentValidity(consent.getConsentId());
    }

    @Override
    public void authenticatePayment(LinksEntity scaLinks) {
        AuthorizationResponse initializePaymentResponse =
                initializeAuthorization(scaLinks.getStartAuthorisation(), credentials);

        AuthorizationResponse authorizationPaymentResponse =
                authorizeWithPassword(
                        initializePaymentResponse.getLinks().getUpdatePsuAuthentication(),
                        credentials);

        authorizeWithOtp(authorizationPaymentResponse);
    }

    private UnicreditConsentResponse createAndSaveConsent() {
        UnicreditConsentResponse consent =
                apiClient.createConsent(strongAuthenticationState.getState());
        storage.saveConsentId(consent.getConsentId());
        return consent;
    }

    private AuthorizationResponse initializeAuthorization(String url, Credentials credentials) {
        return apiClient.initializeAuthorization(
                url, strongAuthenticationState.getState(), credentials.getField(Key.USERNAME));
    }

    private AuthorizationResponse authorizeWithPassword(String url, Credentials credentials) {
        return apiClient.authorizeWithPassword(
                url, credentials.getField(Key.USERNAME), credentials.getField(Key.PASSWORD));
    }

    private void authorizeWithOtp(AuthorizationResponse authorizationResponse) {
        String otp =
                collectOtp(
                        authorizationResponse.getChosenScaMethod(),
                        authorizationResponse.getChallengeData());
        apiClient.finalizeAuthorization(
                authorizationResponse.getLinks().getAuthoriseTransaction(), otp);
    }

    protected String collectOtp(ScaMethodEntity scaMethod, ChallengeDataEntity challengeData) {
        List<Field> fields = embeddedFieldBuilder.getOtpFields(scaMethod, challengeData);
        Map<String, String> supplementalInformation =
                supplementalInformationController.askSupplementalInformationSync(
                        fields.toArray(new Field[0]));
        String inputFieldName =
                fields.stream()
                        .filter(f -> !f.isImmutable())
                        .map(Field::getName)
                        .findFirst()
                        .orElse(null);
        String otp = supplementalInformation.get(inputFieldName);
        if (otp == null) {
            throw SupplementalInfoError.NO_VALID_CODE.exception(
                    "Supplemental info did not come with otp code!");
        } else {
            return otp;
        }
    }

    private void verifyConsentValidity(String consentId) {
        ConsentDetailsResponse consentDetailsResponse = apiClient.getConsentDetails(consentId);
        if (!consentDetailsResponse.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        LocalDate now = LocalDate.now();
        log.info(
                "[UnicreditAuthDebug]: setting credentials expiry to: {} (now is: {})",
                consentDetailsResponse.getValidUntil(),
                now);
        credentials.setSessionExpiryDate(consentDetailsResponse.getValidUntil());
    }
}
