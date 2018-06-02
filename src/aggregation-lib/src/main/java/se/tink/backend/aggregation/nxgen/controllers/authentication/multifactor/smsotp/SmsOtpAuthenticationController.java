package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.i18n.Catalog;

public class SmsOtpAuthenticationController<T> implements MultiFactorAuthenticator {
    private static final int DEFAULT_OTP_VALUE_LENGTH = 4;
    private static final String OTP_VALUE_FIELD_KEY = "otpValue";

    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;
    private final SmsOtpAuthenticator<T> authenticator;
    private final int otpValueLength;

    public SmsOtpAuthenticationController(Catalog catalog,
            SupplementalInformationController supplementalInformationController, SmsOtpAuthenticator authenticator) {
        this(catalog, supplementalInformationController, authenticator, DEFAULT_OTP_VALUE_LENGTH);
    }

    public SmsOtpAuthenticationController(Catalog catalog,
            SupplementalInformationController supplementalInformationController,
            SmsOtpAuthenticator<T> authenticator, int otpValueLength) {
        this.catalog = Preconditions.checkNotNull(catalog);
        this.supplementalInformationController = Preconditions.checkNotNull(supplementalInformationController);
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.otpValueLength = otpValueLength;
    }

    @Override
    public CredentialsTypes getType() {
        // TODO: Change to a multifactor type when supported
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(!Objects.equals(credentials.getType(), getType()),
                String.format("Authentication method not implemented for CredentialsType: %s", credentials.getType()));

        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        T initValues = authenticator.init(username, password);

        Map<String, String> supplementalInformation = supplementalInformationController
                .askSupplementalInformation(getOtpField());

        authenticator.authenticate(supplementalInformation.get(OTP_VALUE_FIELD_KEY), initValues);
    }

    private Field getOtpField() {
        Field otpValue = new Field();
        otpValue.setDescription(catalog.getString("SMS code"));
        otpValue.setName(OTP_VALUE_FIELD_KEY);
        otpValue.setNumeric(true);
        otpValue.setMinLength(otpValueLength);
        otpValue.setMaxLength(otpValueLength);
        otpValue.setHint(StringUtils.repeat("N", otpValueLength));
        otpValue.setPattern(String.format("([0-9]{%d})", otpValueLength));
        otpValue.setPatternError("The code you entered is not valid");

        return otpValue;
    }
}
