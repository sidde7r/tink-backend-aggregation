package se.tink.backend.aggregation.agents.utils.supplementalfields;

import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class DanishFields {

    public static class NemIdInfo {
        private static final String FIELD_KEY = "nemidInfoField";
        private static final LocalizableKey VALUE =
                new LocalizableKey(
                        "Please open the NemId app and confirm login. Then click the \"Submit\" button");

        public static Field build(Catalog catalog) {
            return CommonFields.Information.build(FIELD_KEY, "", catalog.getString(VALUE), null);
        }
    }
}
