package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class VolksbankConstants {

    public static final String UTF_8 = "UTF-8";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final DateFormat DATEFORMAT = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzz)",
            Locale.US);
    public static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Mobile/14E304 EBPMG";
    public static final String CREDENTIAL_USERNUMBER = "usernumber";
    private VolksbankConstants() {
        throw new IllegalStateException();
    }

    public static class Url {
        private static final String BASE = "https://banking.volksbank.at/banking/";
        public static final String LOGIN = BASE + "login.xhtml";
        static final String EXTENSIONS = BASE + "extensions.xhtml";
        static final String MAIN = BASE + "main.xhtml";
        static final String GENERATE_BINDING = BASE + "geraetebindung.xhtml";
        static final String DASHBOARD = BASE + "dashboard";
        public static final String MOBILEAUTH = BASE + "mobileauth.xhtml";
        static final String MOBILEDEVICES = BASE + "rest/mobiledevices";
    }

    public static class Crypto {

        static final String RSA_MODULUS = "00896f66fe79c41130a5c7ad5543a83c890d9fc36c1fd00ee5264d58f45f70a8213af"
                + "36dbf63dba7de7cd48587177de6307769d2e602a886609b944856aed3eb90c5724b744568f47395fa7c36256c854d12fabd"
                + "557086a561c36742d731b4eb93d441b24b350b33bb2eff46289016c73f2434aca550b019b404ed8cd2e3a3e154d6a1b46ce"
                + "b38abfabb291634bfdc16631d03a56b70294a0b2fadfa854635b70c911dc025f4db1eb702911e4e4df84416573becd0f680"
                + "821006d185f350390b795faf392719d8f796b78ec4564c6b6e241b3e837a9486acadecb54ba84965e5ff3a99773ef6acae6"
                + "4d820b4c7b1e243055324156f8eb7f652dd0300c4cfe4d4a7";
        static final String RSA_EXPONENT = "010001";
        static final String RSA_SALT = " ";
        static final String OTP_ALGO = "Totp";
    }

    public static class Form {
        public static final String JSF_VIEWSTATE_KEY = "javax.faces.ViewState";
        public static final String JSF_EVENT_KEY = "javax.faces.behavior.event";
        public static final String JSF_EVENT_ACTION = "action";
        public static final String JSF_EVENT_CHANGE = "change";
        public static final String JSF_EVENT_CLICK = "click";
        public static final String JSF_SOURCE_KEY = "javax.faces.source";
        public static final String JSF_PARTIAL_AJAX_KEY = "javax.faces.partial.ajax";
        public static final String JSF_PARTIAL_EXECUTE_KEY = "javax.faces.partial.execute";
        public static final String JSF_PARTIAL_RENDER_KEY = "javax.faces.partial.render";
        public static final String JSF_PARTIAL_RESET_KEY = "javax.faces.partial.resetValues";
        public static final String GBFORM = "gbform";
        public static final String SHORTPIN_SWITCH_KEY = "shortpin-switch";
        public static final String GBFORM_SUBMIT_KEY = "gbform_SUBMIT";
        public static final String MBEFORM_SUBMIT_KEY = "mbe-form_SUBMIT";
        public static final String MBEFORM = "mbe-form";
        public static final String OTPLOGINOTPFORM = "otplogin:otpform";
        public static final String LOGINFORM = "loginform";
        public static final String SECRET_NAME_KEY = "secretName";
        public static final String SECRET_NAME_VALUE = "iPhone7,1";
        public static final String START_SITE = "startseite";
    }

    static class QueryParam {
        static final String M_KEY = "m";
        static final String M_VALUE = "101";
        static final String A_KEY = "a";
        static final String A_VALUE = "1.13.0";
        static final String QUICK_KEY = "quick";
        static final String QUICK_VALUE = "startseite";
        static final String KEEPSESSION_KEY = "keepsession";
        static final String KEEPSESSION_VALUE = TRUE;
        static final String UPGRADE_KEY = "upgrade";
    }

    public static class Header {
        public static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
        static final String ACCEPT_LANGUAGE_KEY = "Accept-Language";
        static final String ACCEPT_LANGUAGE_VALUE = "en-us";
        static final String ACCEPT_ENCODING_KEY = "Accept-Encoding";
        static final String ACCEPT_ENCODING_VALUE = "gzip, deflate";
        static final String REFERER_KEY = "Referer";
        static final String FACES_REQUEST_KEY = "Faces-Request";
        static final String FACES_REQUEST_VALUE = "partial/ajax";
        static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded; charset=UTF-8";
        static final String ORIGIN_KEY = "Origin";
        static final String ORIGIN_VALUE = "https://banking.volksbank.at";
        static final String TYPE_JSON = "application/json; charset=UTF-8";
    }

    public static class Body {
        static final String VIEW_STATE_ID = "j_id__v_0:javax.faces.ViewState:1";

        static final String GBFORM_ELEMENT_ID = "gbform";
        static final String INPUT_TAG = "input";
        static final String UPDATE_TAG = "update";
        static final String MESSAGE_FALLBACK_ID = "messagesfallback";
    }

    public static class Storage {
        public static final String SECRET = "secret";
        public static final String GENERATE_ID = "generate_id";
        public static final String VIEWSTATE = "viewState";
        public static final String PUSHTOKEN = "pushToken";
    }

    public static class Errors {
        public static final String INVALID_ACCESS = "bitte überprüfen sie ihre zugangsdaten und versuchen sie es erneut";
    }
}
