package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.http.URL;

public class LaBanquePostaleConstants {

    public static class Regex {

        static final String NUMPAD_QUERY_GROUP_NAME = "numpadquery";
        public static final String ERROR_CODE_GROUP_NAME = "errorcode";
        static final Pattern NUMPAD_QUERY_PATTERN = Pattern.compile(
                String.format("(?:background:url\\(loginform\\?)(?<%s>.+)(?:\\))",
                        NUMPAD_QUERY_GROUP_NAME));
        public static final Pattern ERROR_REDIRECT_PATTERN = Pattern.compile(
                String.format("(?:param=)(?<%s>0x\\w+)(?:&|$)",
                        ERROR_CODE_GROUP_NAME));
    }

    private static class ApiServices {

        static final String KEEP_ALIVE_PATH =
                "/ws_qh5/bad/mobile/rest/api/v2/clients/A2G/statut-abonnements";
        static final String INIT_LOGIN_PATH = "/wsost/OstBrokerWeb/pagehandler";
        static final String GET_NUMPAD_PATH = "wsost/OstBrokerWeb/loginform";
        static final String SUBMIT_LOGIN_PATH = "wsost/OstBrokerWeb/auth";
    }

    public static class Urls {

        private static final String BASE = "https://m.labanquepostale.fr/";

        public static final URL KEEP_ALIVE = new URL(BASE + ApiServices.KEEP_ALIVE_PATH);
        public static final URL INIT_LOGIN = new URL(BASE + ApiServices.INIT_LOGIN_PATH);
        public static final URL GET_NUMPAD = new URL(BASE + ApiServices.GET_NUMPAD_PATH);
        public static final URL SUBMIT_LOGIN = new URL(BASE + ApiServices.SUBMIT_LOGIN_PATH);
    }

    public static class QueryParams {

        public static final String TAM_OP = "TAM_OP";
        public static final String ERROR_CODE = "ERROR_CODE";
        public static final String URL = "URL";
        public static final String URL_BACKEND = "urlbackend";
        public static final String ORIGIN = "origin";
        public static final String PASSWORD = "password";
        public static final String USERNAME = "username";
        public static final String CV = "cv";
        public static final String CVVS = "cvvs";
    }

    public static class QueryDefaultValues {

        public static final String LOGIN = "login";
        public static final String ZERO = "0x00000000";
        public static final String TACTILE = "tactile";
        public static final String TRUE = "true";
        public static final String AUTH_INIT = "/ws_qh5/bad/mobile/canalJSON/authentification"
                + "/vide-identif.ea?origin=tactile&codeMedia=9241&version=06_00_01.004";
        public static final String SUBMIT_AUTH =
                "%2Fws_qh5%2Fbad%2Fmobile%2FcanalJSON%2Fauthentification%2Fvide-identif"
                        + ".ea%3Forigin%3Dtactile%26codeMedia%3D9241%26version%3D06_00_01.004";
    }

    public static class AuthConfig {

        public static final int PASSWORD_LENGTH = 10;
        public static final int NUMPAD_WIDTH = 4;
        public static final int NUMPAD_HEIGHT = 4;
        public static final int NUMPAD_KEY_PADDING = 4;
    }

    public static class ErrorMessages {

        public static final String DUPLICATE_DIGITS = "OCR returned duplicate digits.";
        public static final String NO_NUMPAD_IMAGE = "Could not read numpad to image.";
        public static final String NO_NUMPAD_PARAMS = "Could not find numpad params in html doc.";
        public static final String UNKNOWN_ERROR = "Unknown error code %s was encountered.";
        public static final String COULD_NOT_HANDLE_REQUEST =
                "Server could not handle the request.";
        public static final String WRONG_DIGIT_COUNT =
                String.format("Wrong number of keys found. Expected: %s",
                        AuthConfig.PASSWORD_LENGTH);
    }

    public static class ErrorCodes {

        public static final String INCORRECT_CREDENTIALS = "0x132120c8";
        public static final String COULD_NOT_HANDLE_REQUEST = "0x13212070";
    }
}
