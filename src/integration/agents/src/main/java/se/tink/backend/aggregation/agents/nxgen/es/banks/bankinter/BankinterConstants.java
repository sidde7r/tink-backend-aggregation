package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter;

public final class BankinterConstants {

    public static final String INTEGRATION_NAME = "bankinter";

    private BankinterConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE = "https://bancaonline.bankinter.com";
        public static final String LOGIN_PAGE = BASE + "/gestion/login.xhtml";
        public static final String LOGIN = BASE + "/gestion/services/auth/login";
        public static final String KEEP_ALIVE = BASE + "/gestion/rest/usuario/numavisos";
        public static final String IDENTITY_DATA = BASE + "/gestion/rest/usuario/datos";
        public static final String GLOBAL_POSITION =
                BASE + "/extracto/secure/extracto_integral.xhtml";
    }

    public static class LoginForm {
        public static final String FORM_ID = "loginForm";
        public static final String USERNAME_FIELD = "uid";
        public static final String PASSWORD_FIELD = "password";
        public static final String ERROR_PANEL = "errorPanel";
        public static final long SUBMIT_TIMEOUT_SECONDS = 5;
    }
}
