package se.tink.backend.aggregation.agents.brokers.nordnet;

import java.util.regex.Pattern;

public class NordnetConstants {
    static class QueryParamValues {
        static final String CLIENT_ID = "MOBILE_IOS_2";
        static final String CLIENT_SECRET = "6C2B9862-7FEE-CBACE053-3757570ADDEF";
    }

    static class Patterns {
        static final Pattern FIND_CODE_FROM_URI = Pattern.compile("\\?code=([a-zA-Z\\d]*)$");
        static final Pattern FIND_SAMLART_FROM_URI = Pattern.compile("SAMLart=([^&]*)");
        static final Pattern FIND_BANKID_URL =
                Pattern.compile(
                        "https://nneid\\.nordnet\\.se/std/method/nordnet\\.se/[a-zA-Z\\d]*/");
    }

    static class AnonymousLoginFormKeys {
        static final String USERNAME = "username";
        static final String PASSWORD = "password";
        static final String SERVICE = "service";
        static final String COUNTRY = "country";
        static final String SESSION_LANGUAGE = "session_lang";
    }

    static class AnonymousLoginFormValues {
        static final String ANONYMOUS = "<<anonymous>>";
        static final String COUNTRY_SE = "SE";
        static final String LANG_EN = "en";
    }

    static class Urls {

        public static final String BANKID_ORDER_SUFFIX = "order";
        public static final String BANKID_COLLECT_SUFFIX = "collect";
        public static final String BANKID_COMPLETE_SUFFIX = "complete";
        static final String BASE_URL = "https://classic.nordnet.se";
        static final String AUTHENTICATION_BASIC_LOGIN_URL =
                BASE_URL + "/api/2/authentication/basic/login";
        static final String AUTHENTICATION_SAML_ARTIFACT =
                BASE_URL + "/api/2/authentication/eid/saml/artifact";
        static final String OAUTH2_AUTHORIZE_URL =
                BASE_URL
                        + "/oauth2/authorize?client_id="
                        + QueryParamValues.CLIENT_ID
                        + "&response_type=code&redirect_uri=https://www.nordnet.se/now/mobile/token.html";
        static final String INIT_LOGIN_SESSION_URL_PASSWORD = BASE_URL + "/api/2/login";
        static final String INIT_LOGIN_SESSION_URL_BANKID =
                INIT_LOGIN_SESSION_URL_PASSWORD + "/anonymous";
        static final String LOGIN_PAGE_URL =
                BASE_URL
                        + "/oauth2/authorize?authType=signin&client_id="
                        + QueryParamValues.CLIENT_ID
                        + "&response_type=code&redirect_uri=nordnet-react://oauth2/authorize-callback";
        static final String LOGIN_BANKID_PAGE_URL =
                BASE_URL + "/api/2/authentication/eid/saml/request?eid_method=sbidAnother";
        static final String FETCH_TOKEN_URL = BASE_URL + "/oauth2/token";
        static final String GET_ACCOUNTS_URL = BASE_URL + "/api/2/accounts";
        static final String GET_ACCOUNTS_INFO_URL = BASE_URL + "/api/2/accounts/%s/info";
        static final String GET_POSITIONS_URL = BASE_URL + "/api/2/accounts/%s/positions";
        static final String GET_CUSTOMER_INFO_URL = BASE_URL + "/api/2/customers/contact_info";
    }

    static final class Errors {
        static final String NEXT_LOGIN_INVALID_LOGIN_PARAMETER =
                "NEXT_LOGIN_INVALID_LOGIN_PARAMETER";
    }

    static final class Session {

        static final String TYPE_ANONYMOUS = "anonymous";
        static final String TYPE_AUTHENTICATED = "authenticated";
    }

    static final class HeaderKeys {

        public static final String NEXT_REFERRER = "NextReferrer";
        static final String NTAG = "ntag";
    }

    static final class Saml {
        static final String ARTIFACT = "artifact";
    }
}
