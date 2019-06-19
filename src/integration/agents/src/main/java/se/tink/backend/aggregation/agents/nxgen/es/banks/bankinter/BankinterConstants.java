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
        public static final String ACCOUNT = BASE + "/extracto/secure/movimientos_cuenta.xhtml";
    }

    public static class LoginForm {
        public static final String FORM_ID = "loginForm";
        public static final String USERNAME_FIELD = "uid";
        public static final String PASSWORD_FIELD = "password";
        public static final String ERROR_PANEL = "errorPanel";
        public static final long SUBMIT_TIMEOUT_SECONDS = 5;
    }

    public static class FormKeys {
        public static final String JSF_VIEWSTATE = "javax.faces.ViewState";
        public static final String JSF_PARTIAL_AJAX = "javax.faces.partial.ajax";
        public static final String JSF_PARTIAL_EXECUTE = "javax.faces.partial.execute";
        public static final String JSF_PARTIAL_RENDER = "javax.faces.partial.render";
        public static final String JSF_SOURCE = "javax.faces.source";
    }

    public static class FormValues {
        public static final String ACCOUNT_HEADER = "movimientos-cabecera";
        public static final String ACCOUNT_DETAILS = "movimientos-cabecera:head-datos-detalle";
        public static final String TRUE = "true";
        public static final String JSF_EXECUTE_ALL = "@all";
    }

    public static class JsfSource {
        public static final String ACCOUNT_INFO = "movimientos-cabecera:j_id1163893164_56ce7f85";
        public static final String TRANSACTIONS = "j_id591600677_4_3cc72c6c:cargaRemotaMovimientos";
    }

    public static class QueryKeys {
        public static final String ACCOUNT_INDEX = "INDEX_CTA";
        public static final String INDEX = "IND";
    }

    public static class QueryValues {
        public static final String INDEX_N = "N";
    }

    public static class HeaderKeys {
        public static final String JSF_REQUEST = "Faces-request";
    }

    public static class HeaderValues {
        public static final String USER_AGENT =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 11_1_1 like Mac OS X) AppleWebKit/604.3.5 (KHTML, like Gecko) Mobile/15B150 6.1.8-223 WRAPPER";
        public static final String JSF_PARTIAL = "partial/ajax";
    }
}
