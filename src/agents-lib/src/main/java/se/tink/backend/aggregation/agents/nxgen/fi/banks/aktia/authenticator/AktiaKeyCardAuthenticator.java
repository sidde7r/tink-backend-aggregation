package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.LoginDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpChallengeAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpChallengeAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpChallengeEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpDevicePinningRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpDevicePinningResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc.OtpInfoEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;

import java.util.Map;

public class AktiaKeyCardAuthenticator implements KeyCardAuthenticator {
    private static final String KEYCARD_VALUE_FIELD_KEY = "keyCardValue";
    private static final int DEFAULT_KEY_CARD_VALUE_LENGTH = 6;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Catalog catalog;
    private final AktiaApiClient apiClient;
    private final Credentials credentials;
    private final SessionStorage sessionStorage;
    private final EncapClient encapClient;

    private AktiaKeyCardAuthenticator(SupplementalInformationHelper supplementalInformationHelper,
                                     Catalog catalog, AktiaApiClient apiClient, Credentials credentials,
                                     SessionStorage sessionStorage, EncapClient encapClient) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.catalog = catalog;
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
        this.encapClient = encapClient;
    }

    public static AktiaKeyCardAuthenticator createKeyCardAuthenticator(
            SupplementalInformationHelper supplementalInformationHelper, Catalog catalog,
            AktiaApiClient apiClient, Credentials credentials, SessionStorage sessionStorage, EncapClient encapClient) {
        return new AktiaKeyCardAuthenticator(
                supplementalInformationHelper, catalog, apiClient, credentials, sessionStorage, encapClient);
    }

    @Override
    public KeyCardInitValues init(String username, String password) throws AuthenticationException, AuthorizationException {
        // Authenticate to set the Authorization header
        try {
            apiClient.authenticate(username, password);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            String loginFailedReason = response.getHeaders().getFirst(
                    AktiaConstants.Authentication.LOGIN_FAILED_REASON_KEY);
            if (loginFailedReason == null) {
                throw new IllegalStateException("Login failed with unknown reason", e);
            }

            switch (loginFailedReason.toLowerCase()) {
                case AktiaConstants.Authentication.INVALID_USERNAME_OR_PASSWORD_1:
                case AktiaConstants.Authentication.INVALID_USERNAME_OR_PASSWORD_2:
                case AktiaConstants.Authentication.INVALID_USERNAME_OR_PASSWORD_3:
                    throw LoginError.INCORRECT_CREDENTIALS.exception();
                case AktiaConstants.Authentication.ACCOUNT_LOCKED:
                    throw AuthorizationError.ACCOUNT_BLOCKED.exception();
                default:
                    throw new IllegalStateException(
                            String.format("Login failed with unknown error [%s]", loginFailedReason), e);
            }
        }

        // Get the login details containing the key card and challenge info
        LoginDetailsResponse loginDetailsResponse = apiClient.loginDetails();
        OtpChallengeEntity otpChallenge = loginDetailsResponse.getOtpChallenge();

        if (otpChallenge == null) {
            throw new IllegalStateException("OtpChallenge was null - cannot login");
        }

        if (!otpChallenge.isOtpRequired()) {
            throw new IllegalStateException("Got Otp is not required - should have been handled by keep alive");
        }

        OtpInfoEntity otpInfo = otpChallenge.getOtpInfo();

        if (otpInfo == null) {
            throw new IllegalStateException("Cannot complete challenge login - Otp info is null");
        }

        return KeyCardInitValues.createFromCardIdAndCardIndex(otpInfo.getCurrentOtpCard(), otpInfo.getNextOtpIndex());
    }

    @Override
    public void authenticate(String code) throws AuthenticationException, AuthorizationException {
        OtpChallengeAuthenticationResponse otpChallengeAuthenticationResponse = apiClient.finalizeChallenge(
                OtpChallengeAuthenticationRequest.createFromOtpCode(code));

        if (!otpChallengeAuthenticationResponse.isOtpAccepted()) {
            throw LoginError.INCORRECT_CHALLENGE_RESPONSE.exception();
        }

        // This contain the next otp challenge index
        OtpInfoEntity otpInfo = otpChallengeAuthenticationResponse.getOtpInfo();
        if (otpInfo == null) {
            throw new IllegalStateException(
                    "Cannot finalize login - Otp info is null - Could not get next Otp challenge index");
        }

        // We need to fetch these before pinning the device
        apiClient.accountsSummary();
        apiClient.productsSummary();

        // == START Device pinning ==
        Map<String, String> challengeResponse = supplementalInformationHelper.askSupplementalInformation(
                getKeyCardIndexField(otpInfo), getKeyCardValueField());

        OtpDevicePinningResponse otpDevicePinningResponse = apiClient.deviceRegistration(
                OtpDevicePinningRequest.createFromOtpCode(challengeResponse.get(KEYCARD_VALUE_FIELD_KEY)));

        // Important to save the next otp challenge index - This is not provided again before next challenge
        sessionStorage.put(AktiaConstants.Session.NEXT_OTP_CHALLENGE_INDEX_KEY,
                otpDevicePinningResponse.getOtpIndex().orElseThrow(
                        () -> new IllegalStateException("Could not get Otp challenge index")));

        encapClient.activateAndAuthenticateUser(otpDevicePinningResponse.getActivationCode());
        // == END Device pinning ==
    }

    private Field getKeyCardIndexField(OtpInfoEntity otpInfo) {
        String helpText = catalog.getString("Input the code from your code card");
        helpText = helpText + String.format(" (%s)", otpInfo.getCurrentOtpCard());

        Field keyIndexField = new Field();
        keyIndexField.setDescription(catalog.getString("Key card index"));
        keyIndexField.setName("keyCardIndex");
        keyIndexField.setHelpText(helpText);
        keyIndexField.setValue(otpInfo.getNextOtpIndex());
        keyIndexField.setImmutable(true);
        return keyIndexField;
    }

    private Field getKeyCardValueField() {
        Field codeCardValue = new Field();
        codeCardValue.setDescription(catalog.getString("Key card code"));
        codeCardValue.setName(KEYCARD_VALUE_FIELD_KEY);
        codeCardValue.setNumeric(true);
        codeCardValue.setMinLength(DEFAULT_KEY_CARD_VALUE_LENGTH);
        codeCardValue.setMaxLength(DEFAULT_KEY_CARD_VALUE_LENGTH);
        codeCardValue.setHint(StringUtils.repeat("N", DEFAULT_KEY_CARD_VALUE_LENGTH));
        codeCardValue.setPattern(String.format("([0-9]{%d})", DEFAULT_KEY_CARD_VALUE_LENGTH));
        codeCardValue.setPatternError("The code you entered is not valid");

        return codeCardValue;
    }
}
