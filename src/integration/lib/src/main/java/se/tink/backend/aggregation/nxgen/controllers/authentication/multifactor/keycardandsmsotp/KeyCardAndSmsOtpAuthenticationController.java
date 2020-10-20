package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycardandsmsotp;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycard.KeyCardInitValues;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsInitResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp.SmsOtpAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n.Catalog;

public class KeyCardAndSmsOtpAuthenticationController<T> implements MultiFactorAuthenticator {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String KEYCARD_VALUE_FIELD_KEY = "keyCardValue";
    private static final String OTP_VALUE_FIELD_KEY = "otpValue";
    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final KeyCardAuthenticator keyCarduthenticator;
    private final SmsOtpAuthenticator<T> smsOtpAuthenticator;
    private final int keyCardValueLength;
    private final int otpValueLength;

    public KeyCardAndSmsOtpAuthenticationController(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            KeyCardAuthenticator keyCarduthenticator,
            int keyCardValueLength,
            SmsOtpAuthenticator<T> smsOtpAuthenticator,
            int otpValueLength) {
        this.catalog = catalog;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.keyCarduthenticator = keyCarduthenticator;
        this.smsOtpAuthenticator = smsOtpAuthenticator;
        this.keyCardValueLength = keyCardValueLength;
        this.otpValueLength = otpValueLength;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        keyCardAuthenticate(credentials);
        smsOtpAuthenticate(credentials);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    private void keyCardAuthenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));

        final String username = credentials.getField(Field.Key.USERNAME);
        final String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        final KeyCardInitValues keyCardInitValues = keyCarduthenticator.init(username, password);

        Map<String, String> supplementalInformation;

        try {
            supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(
                            getKeyCardIndexField(keyCardInitValues), getKeyCardValueField());
        } catch (SupplementalInfoException ex) {
            throw SupplementalInfoError.NO_VALID_CODE.exception();
        }

        keyCarduthenticator.authenticate(supplementalInformation.get(KEYCARD_VALUE_FIELD_KEY));
    }

    private Field getKeyCardIndexField(KeyCardInitValues keyCardInitValues) {
        final Optional<String> keyCardId = keyCardInitValues.getCardId();
        final String keyCardCodeIndex = keyCardInitValues.getCardIndex();

        String helpText = catalog.getString("Input the code from your code card");
        if (keyCardId.isPresent()) {
            helpText = helpText + String.format(" (%s)", keyCardId.get());
        }

        return Field.builder()
                .description(catalog.getString("Key card index"))
                .name("keyCardIndex")
                .helpText(helpText)
                .value(keyCardCodeIndex)
                .immutable(true)
                .build();
    }

    private Field getKeyCardValueField() {
        return Field.builder()
                .description(catalog.getString("Key card code"))
                .name(KEYCARD_VALUE_FIELD_KEY)
                .numeric(true)
                .minLength(keyCardValueLength)
                .maxLength(keyCardValueLength)
                .hint(StringUtils.repeat("N", keyCardValueLength))
                .pattern(String.format("([0-9]{%d})", keyCardValueLength))
                .patternError("The code you entered is not valid")
                .build();
    }

    private void smsOtpAuthenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        final String username = credentials.getField(Field.Key.USERNAME);
        SmsInitResult<T> smsInitResult = smsOtpAuthenticator.init(username);

        if (smsInitResult.isRequired()) {
            final Map<String, String> supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(getOtpField());

            smsOtpAuthenticator.authenticate(
                    supplementalInformation.get(OTP_VALUE_FIELD_KEY),
                    username,
                    smsInitResult.getToken());
        } else {
            LOGGER.info("Skipping SMS authentication");
        }
        smsOtpAuthenticator.postAuthentication();
    }

    private Field getOtpField() {
        return Field.builder()
                .description(catalog.getString("Verification code"))
                .name(OTP_VALUE_FIELD_KEY)
                .numeric(true)
                .minLength(otpValueLength)
                .maxLength(otpValueLength)
                .hint(StringUtils.repeat("N", otpValueLength))
                .pattern(String.format("([0-9]{%d})", otpValueLength))
                .patternError("The code you entered is not valid")
                .build();
    }
}
