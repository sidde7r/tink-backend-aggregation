package se.tink.backend.aggregation.agents.utils.supplementalfields;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Field;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BalticFields {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SmartIdChallengeCode {
        private static final String FIELD_KEY = "name";
        private static final LocalizableKey DESCRIPTION = new LocalizableKey("Challenge code");
        private static final LocalizableKey HELP_TEXT =
                new LocalizableKey(
                        "Open SmartID on your mobile device, check that the challenge codes match and enter the PIN. Then continue the connection.");

        public static Field build(Catalog catalog, String challengeCode) {
            return CommonFields.Information.build(
                    FIELD_KEY,
                    catalog.getString(DESCRIPTION),
                    challengeCode,
                    catalog.getString(HELP_TEXT));
        }
    }
}
