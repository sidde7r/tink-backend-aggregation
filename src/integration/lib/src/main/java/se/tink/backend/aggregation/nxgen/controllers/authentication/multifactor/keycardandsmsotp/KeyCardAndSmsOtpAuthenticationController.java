package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycardandsmsotp;

import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Objects;
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
import se.tink.backend.aggregation.agents.utils.supplementalfields.CommonFields;
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

        keyCarduthenticator.authenticate(
                supplementalInformation.get(CommonFields.KeyCardCode.FIELD_KEY));
    }

    private Field getKeyCardIndexField(KeyCardInitValues keyCardInitValues) {
        return CommonFields.KeyCardInfo.build(
                catalog,
                keyCardInitValues.getCardIndex(),
                keyCardInitValues.getCardId().orElse(null));
    }

    private Field getKeyCardValueField() {
        return CommonFields.KeyCardCode.build(catalog, keyCardValueLength);
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
