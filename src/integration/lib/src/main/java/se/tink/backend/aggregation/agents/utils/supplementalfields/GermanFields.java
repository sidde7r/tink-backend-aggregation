package se.tink.backend.aggregation.agents.utils.supplementalfields;

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
            Field.Builder otpBuilder =
                    Field.builder()
                            .description(catalog.getString(DESCRIPTION))
                            .helpText(catalog.getString(HELPTEXT))
                            .immutable(true)
                            .name(FIELD_KEY)
                            .value(startcode);

            return otpBuilder.build();
        }
    }

    public static class Tan {

        enum OTP_TYPE {
            INTEGER,
            CHARACTERS
        }

        private static final String FIELD_KEY = "tanField";
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

        public static String getFieldKey() {
            return FIELD_KEY;
        }

        public static Field build(
                Catalog catalog, String scaMethodName, Integer otpMaxLength, String otpFormat) {
            String helpText =
                    scaMethodName != null
                            ? catalog.getString(HELPTEXT_WITH_NAME_FORMAT, scaMethodName)
                            : catalog.getString(HELPTEXT);

            Field.Builder otpBuilder =
                    Field.builder()
                            .name(FIELD_KEY)
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
                otpBuilder.pattern("^[0-9]{1," + otpMaxLength + "}$");
            }
            otpBuilder
                    .numeric(true)
                    .patternError(catalog.getString(NUMERIC_OTP_PATTERN_ERROR, otpMaxLength));
        }

        private static void prepareOtpCharactersFormat(
                Catalog catalog, Integer otpMaxLength, Field.Builder otpBuilder) {
            if (otpMaxLength != null) {
                otpBuilder.pattern("^[^\\s]{1," + otpMaxLength + "}$");
            }
            otpBuilder.patternError(catalog.getString(CHARACTERS_OTP_PATTERN_ERROR, otpMaxLength));
        }
    }
}
