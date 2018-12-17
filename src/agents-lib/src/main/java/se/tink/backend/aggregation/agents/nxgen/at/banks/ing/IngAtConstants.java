package se.tink.backend.aggregation.agents.nxgen.at.banks.ing;

import se.tink.backend.aggregation.nxgen.http.URL;

public class IngAtConstants {
    public static final class Url {
        public static final URL AUTH_START = new URL("https://banking.ing.at/online-banking/");
        public static final URL LOGOUT = new URL("https://banking.ing.at/online-banking/wicket/logout");
        public static final URL ACCOUNT_PREFIX = new URL("https://banking.ing.at/online-banking/wicket/wicket/");
        public static final URL PASSWORD = new URL("https://banking.ing.at/online-banking/wicket/login?0-1.IFormSubmitListener-login~card-password_form");
    }

    public static class Header {
        public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:63.0) Gecko/20100101 Firefox/63.0";
    }

    public enum Storage {
        ACCOUNT_INDEX,
        CURRENT_URL,
        WEB_LOGIN_RESPONSE;
    }
}
