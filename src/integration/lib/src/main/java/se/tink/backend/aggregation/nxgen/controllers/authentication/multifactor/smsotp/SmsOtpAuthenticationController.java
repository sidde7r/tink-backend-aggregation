package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp;

import com.google.common.base.Preconditions;
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
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.i18n.Catalog;

public class SmsOtpAuthenticationController<T> implements TypedAuthenticator {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String OTP_VALUE_FIELD_KEY = "otpValue";

    private final Catalog catalog;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SmsOtpAuthenticator<T> authenticator;
    private final int otpValueLength;

    public SmsOtpAuthenticationController(
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            SmsOtpAuthenticator<T> authenticator,
            int otpValueLength) {
        this.catalog = Preconditions.checkNotNull(catalog);
        this.supplementalInformationHelper =
                Preconditions.checkNotNull(supplementalInformationHelper);
        this.authenticator = Preconditions.checkNotNull(authenticator);
        Preconditions.checkArgument(otpValueLength > 0);
        this.otpValueLength = otpValueLength;
    }

    @Override
    public CredentialsTypes getType() {
        // TODO: Change to a multifactor type when supported
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        NotImplementedException.throwIf(
                !Objects.equals(credentials.getType(), getType()),
                String.format(
                        "Authentication method not implemented for CredentialsType: %s",
                        credentials.getType()));

        String username = credentials.getField(Field.Key.USERNAME);

        if (Strings.isNullOrEmpty(username)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        SmsInitResult<T> smsInitResult = authenticator.init(username);

        if (smsInitResult.isRequired()) {
            Map<String, String> supplementalInformation =
                    supplementalInformationHelper.askSupplementalInformation(getOtpField());

            authenticator.authenticate(
                    supplementalInformation.get(OTP_VALUE_FIELD_KEY),
                    username,
                    smsInitResult.getToken());
        } else {
            LOGGER.info("Skipping SMS authentication");
        }
        authenticator.postAuthentication();
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
