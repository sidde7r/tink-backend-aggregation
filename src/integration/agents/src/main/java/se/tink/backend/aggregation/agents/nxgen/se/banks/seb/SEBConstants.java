package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

public class SEBConstants {

    public static class Urls {
        public static final String BASE = "https://mp.seb.se";

        public static final URL FETCH_AUTOSTART_TOKEN =
                new URL(BASE + Endpoints.FETCH_AUTOSTART_TOKEN);
        public static final URL COLLECT_BANKID = new URL(BASE + Endpoints.COLLECT_BANKID);
    }

    public static class Endpoints {
        public static final String FETCH_AUTOSTART_TOKEN = "/nauth2/Authentication/api/v1/bid/auth";
        public static final String COLLECT_BANKID = "/nauth2/Authentication/api/v1/bid/";
    }

    public static class HeaderKeys {
        public static final String X_SEB_UUID = "x-seb-uuid";
    }

    public static class RequestBody {
        public static final String SEB_REFERER_VALUE = "/masp/mbid";
    }

    public static class LoginCodes {
        // Strings for status comparison when logging in with BankID.
        public static final String START_BANKID = "RFA1";
        public static final String ALREADY_IN_PROGRESS = "RFA3";
        public static final String USER_CANCELLED = "RFA6";
        public static final String NO_CLIENT = "RFA8";
        public static final String USER_SIGN = "RFA9";
        public static final String AUTHENTICATED = "RFA100";
        public static final String AUTHORIZATION_REQUIRED = "RFA101";
        public static final String COLLECT_BANKID = "RFA102";
    }

    public static class ErrorMessages {
        public static final String UNKNOWN_BANKID_STATUS = "Unknown BankIdStatus (%s)";
    }

    public enum UserMessage implements LocalizableEnum {
        MUST_AUTHORIZE_BANKID(
                new LocalizableKey(
                        "The first time you use your mobile BankId you have to verify it with your Digipass. Login to the SEB-app with your mobile BankID to do this."));

        private final LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
