package se.tink.backend.aggregation.agents.utils.supplementalfields;

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
        private static final String FIELD_KEY = "tanField";
        private static final LocalizableKey DESCRIPTION = new LocalizableKey("TAN");
        private static final LocalizableParametrizedKey HELPTEXT_WITH_NAME_FORMAT =
                new LocalizableParametrizedKey(
                        "Confirm by entering the generated TAN for \"{0}\".");
        private static final LocalizableKey HELPTEXT =
                new LocalizableKey("Confirm by entering the generated TAN.");

        public static String getFieldKey() {
            return FIELD_KEY;
        }

        public static Field build(Catalog catalog, String scaMethodName) {
            String helpText =
                    scaMethodName != null
                            ? catalog.getString(HELPTEXT_WITH_NAME_FORMAT, scaMethodName)
                            : catalog.getString(HELPTEXT);

            return Field.builder()
                    .name(FIELD_KEY)
                    .description(catalog.getString(DESCRIPTION))
                    .helpText(helpText)
                    .minLength(1)
                    .build();
        }
    }
}
