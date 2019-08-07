package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo;

import java.time.ZoneId;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

public class MonzoConstants {
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/London");
    public static final String INTEGRATION_NAME = "monzo";
    public static final String UK_RETAIL = "uk_retail";
    public static final String UK_RETAIL_JOINT = "uk_retail_joint";

    public static final AccountTypeMapper ACCOUNT_TYPE =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, UK_RETAIL, UK_RETAIL_JOINT)
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public class Urls {
        public static final String AUTH_MONZO_COM = "https://auth.monzo.com/";
        private static final String API_MONZO_COM = "https://api.monzo.com/";
        public static final String OAUTH2_TOKEN = API_MONZO_COM + "oauth2/token";
        public static final String AIS_ACCOUNTS = API_MONZO_COM + "ais/accounts";
        public static final String AIS_BALANCE = API_MONZO_COM + "ais/balance";
        public static final String AIS_TRANSACTIONS = API_MONZO_COM + "ais/transactions";

        class V31 {
            public static final String AIS_AUTH_URL =
                    "https://openbanking.monzo.com/open-banking/v3.1/aisp";
            public static final String AIS_API_URL =
                    "https://openbanking.monzo.com/open-banking/v3.1/aisp";
            public static final String PIS_AUTH_URL =
                    "https://openbanking.monzo.com/open-banking/v3.1/pisp";
            public static final String PIS_API_URL =
                    "https://openbanking.monzo.com/open-banking/v3.1/pisp";
            public static final String WELL_KNOWN_URL =
                    "https://api.monzo.com/open-banking/.well-known/openid-configuration";
        }
    }

    public class RequestKey {
        public static final String ACCOUNT_ID = "account_id";
        public static final String LIMIT = "limit";
        public static final String SINCE = "since";
        public static final String BEFORE = "before";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String CODE = OAuth2Constants.CallbackParams.CODE;
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public class RequestValue {
        public static final String CODE = OAuth2Constants.CallbackParams.CODE;
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public class StorageKey {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REDIRECT_URL = "redirect_url";
    }
}
