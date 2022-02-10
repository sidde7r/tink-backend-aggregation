package se.tink.backend.aggregation.agents.utils.supplementalfields;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.OtpFormat;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.i18n_aggregation.LocalizableParametrizedKey;

@RequiredArgsConstructor
public class TanBuilder {

    /**
     * IMPORTANT!!! FieldName is used by SDK Team on Frontend - to have specific screen depending on
     * authentication type. Try not to change the field names.
     *
     * <p>If you need to change field names - please inform SDK Team to support them.
     */
    private static final Map<AuthenticationType, String> AUTH_TYPE_TO_FIELD_NAME =
            new ImmutableMap.Builder<AuthenticationType, String>()
                    .put(AuthenticationType.SMS_OTP, "smsTan")
                    .put(AuthenticationType.CHIP_OTP, "chipTan")
                    .put(AuthenticationType.PHOTO_OTP, "photoTan")
                    .put(AuthenticationType.PUSH_OTP, "pushTan")
                    .put(AuthenticationType.SMTP_OTP, "smtpTan")
                    .build();

    private static final String DEFAULT_FIELD_NAME = "tanField";

    private static final LocalizableKey DESCRIPTION = new LocalizableKey("TAN");

    private static final LocalizableParametrizedKey HELPTEXT_WITH_NAME_FORMAT =
            new LocalizableParametrizedKey("Confirm by entering the generated TAN for \"{0}\".");
    private static final LocalizableKey HELPTEXT =
            new LocalizableKey("Confirm by entering the generated TAN.");

    private static final Map<OtpFormat, LocalizableParametrizedKey> OTP_FROMAT_TO_PATTERN_ERROR =
            new ImmutableMap.Builder<OtpFormat, LocalizableParametrizedKey>()
                    .put(
                            OtpFormat.INTEGER,
                            new LocalizableParametrizedKey("Please enter a maximum of {0} digits"))
                    .put(
                            OtpFormat.CHARACTERS,
                            new LocalizableParametrizedKey(
                                    "Please enter a maximum of {0} characters"))
                    .build();

    private final Catalog catalog;
    private String authenticationMethodName;
    private String authenticationType;
    private int otpMinLength = 1;
    private Integer otpMaxLength;
    private OtpFormat otpFormat;

    public TanBuilder authenticationMethodName(String authenticationMethodName) {
        this.authenticationMethodName = authenticationMethodName;
        return this;
    }

    public TanBuilder authenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
        return this;
    }

    public TanBuilder otpMinLength(int otpMinLength) {
        this.otpMinLength = otpMinLength;
        return this;
    }

    public TanBuilder otpMaxLength(Integer otpMaxLength) {
        this.otpMaxLength = otpMaxLength;
        return this;
    }

    public TanBuilder otpFormat(OtpFormat otpFormat) {
        this.otpFormat = otpFormat;
        return this;
    }

    public Field build() {
        String fieldName =
                AuthenticationType.fromString(authenticationType)
                        .map(x -> AUTH_TYPE_TO_FIELD_NAME.getOrDefault(x, DEFAULT_FIELD_NAME))
                        .orElse(DEFAULT_FIELD_NAME);

        String helpText =
                authenticationMethodName != null
                        ? catalog.getString(HELPTEXT_WITH_NAME_FORMAT, authenticationMethodName)
                        : catalog.getString(HELPTEXT);

        Field.Builder otpBuilder =
                Field.builder()
                        .name(fieldName)
                        .description(catalog.getString(DESCRIPTION))
                        .helpText(helpText)
                        .minLength(otpMinLength)
                        .numeric(otpFormat == OtpFormat.INTEGER);

        if (otpMaxLength != null) {
            otpBuilder.hint(StringUtils.repeat("_", " ", otpMaxLength));
            otpBuilder.maxLength(otpMaxLength);
            if (otpFormat != null) {
                otpBuilder.pattern(buildPattern());
                otpBuilder.patternError(
                        catalog.getString(
                                OTP_FROMAT_TO_PATTERN_ERROR.get(otpFormat), otpMaxLength));
            }
        }
        return otpBuilder.build();
    }

    private String buildPattern() {
        if (otpFormat == null || otpMaxLength == null) {
            return null;
        }

        String allowedCharacters = otpFormat == OtpFormat.INTEGER ? "[0-9]" : "[^\\s]";
        String count =
                otpMinLength == otpMaxLength
                        ? "{" + otpMinLength + "}"
                        : "{" + otpMinLength + "," + otpMaxLength + "}";

        return "^" + allowedCharacters + count + "$";
    }
}
