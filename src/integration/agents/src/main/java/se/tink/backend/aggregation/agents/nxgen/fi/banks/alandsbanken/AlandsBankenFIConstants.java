package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.libraries.i18n_aggregation.LocalizableEnum;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class AlandsBankenFIConstants {

    public static final class Url {
        public static final String BASE =
                "https://mob.alandsbanken.fi/cbs-inet-json-api-aab-v1/api/";
    }

    public static final class Fetcher {
        public static final LogTag TRANSACTION_LOGGING = LogTag.from("#transaction_alandsbank_fi");
    }

    public enum EndUserMessage implements LocalizableEnum {
        PASSWORD_EXPIRED(
                new LocalizableKey(
                        "Your current password has expired and you have to create a new one."
                                + " You can do this in Ålandsbanken's app if you have a registered device, if not, please contact "
                                + "Ålandsbanken at: 0204 292 910."));

        private LocalizableKey userMessage;

        EndUserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
