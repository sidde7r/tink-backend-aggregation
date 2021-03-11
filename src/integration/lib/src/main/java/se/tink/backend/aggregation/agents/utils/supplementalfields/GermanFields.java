package se.tink.backend.aggregation.agents.utils.supplementalfields;

import com.google.common.base.Enums;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.i18n.LocalizableParametrizedKey;

public class GermanFields {

    public static class Startcode {

        private static final String FIELD_KEY = "startcodeField";

        private static final LocalizableKey DESCRIPTION = new LocalizableKey("Startcode");
        private static final LocalizableKey HELPTEXT =
                new LocalizableKey(
                        "Insert your girocard into the TAN-generator and press \"TAN\". Enter the startcode and press \"OK\".");

        public static Field build(Catalog catalog, String startcode) {
            return CommonFields.Information.build(
                    FIELD_KEY,
                    catalog.getString(DESCRIPTION),
                    startcode,
                    catalog.getString(HELPTEXT));
        }
    }

    public static class Tan {

        enum OTP_TYPE {
            INTEGER,
            CHARACTERS
        }

        @RequiredArgsConstructor
        private enum AuthenticationType {
            SMS_OTP("smsTan"),
            CHIP_OTP("chipTan"),
            PUSH_OTP("pushTan"),
            PHOTO_OTP("photoTan"),
            SMTP_OTP("smtpTan"),
            UNKNOWN_OTP("tanField");

            /**
             * IMPORTANT!!! FieldName is used by SDK Team on Frontend - to have specific screen
             * depending on authentication type. Try not to change the field names.
             *
             * <p>If you need to change field names - please inform SDK Team to support them.
             */
            @Getter(AccessLevel.PRIVATE)
            private final String fieldName;

            private static AuthenticationType getOrDefault(String authenticaionType) {
                if (StringUtils.isEmpty(authenticaionType)) {
                    return UNKNOWN_OTP;
                }
                return Enums.getIfPresent(AuthenticationType.class, authenticaionType.toUpperCase())
                        .or(UNKNOWN_OTP);
            }
        }

        private static final LocalizableKey DESCRIPTION = new LocalizableKey("TAN");

        private static final LocalizableParametrizedKey HELPTEXT_WITH_NAME_FORMAT =
                new LocalizableParametrizedKey(
                        "Confirm by entering the generated TAN for \"{0}\".");
        private static final LocalizableKey HELPTEXT =
                new LocalizableKey("Confirm by entering the generated TAN.");

        private static final LocalizableParametrizedKey NUMERIC_OTP_PATTERN_ERROR =
                new LocalizableParametrizedKey("Please enter a maximum of {0} digits");
        private static final LocalizableParametrizedKey CHARACTERS_OTP_PATTERN_ERROR =
                new LocalizableParametrizedKey("Please enter a maximum of {0} characters");

        public static Field build(
                Catalog catalog,
                String authenticationType,
                String scaMethodName,
                Integer otpMaxLength,
                String otpFormat) {
            String helpText =
                    scaMethodName != null
                            ? catalog.getString(HELPTEXT_WITH_NAME_FORMAT, scaMethodName)
                            : catalog.getString(HELPTEXT);

            Field.Builder otpBuilder =
                    Field.builder()
                            .name(
                                    AuthenticationType.getOrDefault(authenticationType)
                                            .getFieldName())
                            .description(catalog.getString(DESCRIPTION))
                            .helpText(helpText)
                            .minLength(1);

            prepareFieldWhenOtpLengthAvailable(otpMaxLength, otpBuilder);

            if (Tan.OTP_TYPE.INTEGER.name().equalsIgnoreCase(otpFormat)) {
                prepareOtpNumericFormat(catalog, otpMaxLength, otpBuilder);
            } else if (Tan.OTP_TYPE.CHARACTERS.name().equalsIgnoreCase(otpFormat)) {
                prepareOtpCharactersFormat(catalog, otpMaxLength, otpBuilder);
            }
            return otpBuilder.build();
        }

        private static void prepareFieldWhenOtpLengthAvailable(
                Integer otpMaxLength, Field.Builder otpBuilder) {
            if (otpMaxLength != null) {
                otpBuilder.hint(StringUtils.repeat("_ ", otpMaxLength)).maxLength(otpMaxLength);
            }
        }

        private static void prepareOtpNumericFormat(
                Catalog catalog, Integer otpMaxLength, Field.Builder otpBuilder) {
            if (otpMaxLength != null) {
                otpBuilder
                        .pattern("^[0-9]{1," + otpMaxLength + "}$")
                        .patternError(catalog.getString(NUMERIC_OTP_PATTERN_ERROR, otpMaxLength));
            }
            otpBuilder.numeric(true);
        }

        private static void prepareOtpCharactersFormat(
                Catalog catalog, Integer otpMaxLength, Field.Builder otpBuilder) {
            if (otpMaxLength != null) {
                otpBuilder
                        .pattern("^[^\\s]{1," + otpMaxLength + "}$")
                        .patternError(
                                catalog.getString(CHARACTERS_OTP_PATTERN_ERROR, otpMaxLength));
            }
        }
    }
}
