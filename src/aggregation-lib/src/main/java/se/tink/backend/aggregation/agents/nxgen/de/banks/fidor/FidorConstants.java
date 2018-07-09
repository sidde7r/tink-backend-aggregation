package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class FidorConstants {

    public static final String BEARER_TOKEN = "Bearer ";
    public static final String BASIC = "Basic ";
    public static final String CURRENCY_EUR = "EUR";

    public static final String SANBOX_CLIENT_ID = "4c7b5996af0ee5db";
    public static final String SANDBOX_BASE64_BASIC_AUTH = "NGM3YjU5OTZhZjBlZTVkYjo2ZTU4MWM0NDBjZjk5ZjE3NjI1YTJlYzZiYzQwNGE4MA=="; //client_id:client_secret
    public static final String SANDBOX_REDIRECT_URL = "http://google.com";
    public static final String STATE = "99MagiCat99";

    public class URL {

        public class OPENAPI {
            public static final String ACCOUNTS = "/accounts";
            public static final String TRANSACTIONS = "/transactions";
            public static final String RATELIMIT = "/rate_limit";
            public static final String OAUTH_AUTHORIZE = "/oauth/authorize";
            public static final String OAUTH_TOKEN = "/oauth/token";
            public static final String UPCOMING_TRANSACTIONS = "/preauths";

            public static final String SANDBOX_BASE = "https://aps.fidor.de";
        }
    }

    public class STORAGE {
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
    }

    public class ERROR {
        public static final String INVALID_CREDENTIALS = "Wrong User or Password";
    }

    public class BODY {

        public class OPENAPI {
            public static final String CLIENT_ID = "client_id";
            public static final String REDIRECT_URI = "redirect_uri";
            public static final String GRANT_TYPE = "grant_type";
            public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
            public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
            public static final String CODE = "code";
            public static final String PAGE = "page";
            public static final String PER_PAGE = "per_page";
            public static final String PER_PAGE_MAXVALUE = "100";
            public static final String REFRESH_TOKEN = "refresh_token";
        }
    }

    public class QUERYPARAMS {
        public static final String CLIENT_ID = "client_id";
        public static final String REDRIECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String RESPONSE_TYPE_CODE = "code";
    }

    public class FORM {
        public static final String EMAIL_ID = "session_email";
        public static final String PASSWORD_ID = "session_password";
        public static final String SUBMIT_NAME = "commit";
    }

    public class HEADERS {
        public class OPENAPI {
            public static final String APPLICATION_JSON_FIDOR_V1 = "application/vnd.fidor.de; version=1,text/json";
        }
    }

    public static class LOGGING {
        public static final LogTag AUTHENTICATION_ERROR = LogTag.from("FIDOR_AUTHENTICATION_ERROR");
    }

}
