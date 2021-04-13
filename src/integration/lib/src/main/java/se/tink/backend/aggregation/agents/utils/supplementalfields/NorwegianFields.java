package se.tink.backend.aggregation.agents.utils.supplementalfields;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NorwegianFields {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BankIdReferenceInfo {
        private static final String FIELD_KEY = "name";
        private static final LocalizableKey DESCRIPTION = new LocalizableKey("Reference");
        private static final LocalizableKey HELP_TEXT =
                new LocalizableKey(
                        "Continue by clicking update when you have verified the reference and signed with Mobile BankID.");

        public static Field build(Catalog catalog, String bankIdReference) {
            return CommonFields.Information.build(
                    FIELD_KEY,
                    catalog.getString(DESCRIPTION),
                    bankIdReference,
                    catalog.getString(HELP_TEXT));
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BankIdCodeChipField {

        private static final String FIELD_KEY = "bankIdChipCode";
        private static final int CODE_LENGTH = 6;

        private static final LocalizableKey DESCRIPTION = new LocalizableKey("Chip code");
        private static final LocalizableKey HELP_TEXT =
                new LocalizableKey("Please enter code generated using your code chip.");

        public static Field build(Catalog catalog) {
            return Field.builder()
                    .name(FIELD_KEY)
                    .helpText(catalog.getString(HELP_TEXT))
                    .description(catalog.getString(DESCRIPTION))
                    .numeric(true)
                    .minLength(CODE_LENGTH)
                    .maxLength(CODE_LENGTH)
                    .hint(StringUtils.repeat("N", CODE_LENGTH))
                    .build();
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BankIdAppField {

        private static final LocalizableKey DESCRIPTION =
                new LocalizableKey("Confirm BankID authentication in your mobile BankID app.");

        public static Field build(Catalog catalog) {
            return CommonFields.Instruction.build(catalog.getString(DESCRIPTION));
        }
    }
}
