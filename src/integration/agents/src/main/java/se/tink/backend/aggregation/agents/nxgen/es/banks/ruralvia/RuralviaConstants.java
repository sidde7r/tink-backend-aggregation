package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia;

public class RuralviaConstants {

    public static final String NATIONAL_ID_NUMBER = "national-id-number";
    public static final String PROVIDER_NAME = "es-ruralvia-password";

    public static class Urls {

        public static final String RURALVIA_HOST = "www.ruralvia.com";
        public static final String RURALVIA_SECURE_HOST = "https://www.ruralvia.com";
        public static final String RURALVIA_HOME =
                "https://www.ruralvia.com/accesodirecto/default.htm";
        public static final String RURALVIA_MOBILE_LOGIN =
                "https://www.ruralvia.com/isum/Main?ISUM_SCR=login&loginType=accesoSeguro&ISUM_Portal=104&acceso_idioma=es_ES";
    }

    public static class LoginForm {
        public static final String USER_FIELD = "divUser";
        public static final String ID_FIELD = "divDoc";
        public static final String PASSWORD_FIELD = "divErrorPasswordParent";
        public static final String WEB_VIEW = "label[for='accessTypeClassic']";
        public static final String ACCEPT_BUTTON = "acceptButton";
    }

    public static class HeaderValues {
        public static final String USER_AGENT =
                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 6.2.2-239 WRAPPER";
        public static final String REQUESTED_WITH = "XMLHttpRequest";
        public static final String ACCEPT_LANGUAGE = "es-ES";
    }

    public static class Tags {
        public static final String TAG_INPUT_UPPERCASE = "INPUT";
        public static final String TAG_INPUT = "input";
        public static final String ATTRIBUTE_TAG_VALUE = "value";
        public static final String ATTRIBUTE_TAG_ACTION = "action";
        public static final String PARSE_CONSTANT_TAMANIOPAGINA = "tamanioPagina";
    }
}
