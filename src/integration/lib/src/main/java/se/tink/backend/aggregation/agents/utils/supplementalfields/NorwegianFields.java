package se.tink.backend.aggregation.agents.utils.supplementalfields;

import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class NorwegianFields {

    public static class BankIdReferenceInfo {
        private static final String FIELD_KEY = "name";
        private static final String DESCRIPTION = "Reference";
        private static final LocalizableKey HELP_TEXT =
                LocalizableKey.of(
                        "Continue by clicking update when you have verified the reference and signed with Mobile BankID.");

        public static Field build(Catalog catalog, String bankIdReference) {
            return CommonFields.Information.build(
                    FIELD_KEY, DESCRIPTION, bankIdReference, catalog.getString(HELP_TEXT));
        }
    }
}
