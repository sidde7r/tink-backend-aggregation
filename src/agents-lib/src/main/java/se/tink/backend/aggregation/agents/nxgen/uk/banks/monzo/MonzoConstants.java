package se.tink.backend.aggregation.agents.nxgen.uk.banks.monzo;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.ZoneId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.rpc.AccountTypes;

public class MonzoConstants {

    public static final ZoneId ZONE_ID = ZoneId.of("Europe/London");

    private static final Logger logger = LoggerFactory.getLogger(MonzoConstants.class);

    public static class AccountType {

        private static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES_MAP = ImmutableMap.<String, AccountTypes>builder()
                .put("uk_retail", AccountTypes.CHECKING)
                .build();

        public static boolean verify(String key, AccountTypes value) {
            Optional<AccountTypes> translated = translate(key);
            return translated.isPresent() && translated.get() == value;
        }

        public static Optional<AccountTypes> translate(String accountType) {
            if (Strings.isNullOrEmpty(accountType)) {
                return Optional.empty();
            }
            Optional<AccountTypes> retVal = Optional.ofNullable(ACCOUNT_TYPES_MAP.get(accountType.toLowerCase()));
            if (!retVal.isPresent()) {
                logger.info("{} for account type: {}", Logging.UNKNOWN_ACCOUNT_TYPE, accountType);
            }
            return retVal;
        }
    }

    public static class Logging {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("uk-monzo-oauth2-unknown-account-type");
    }

    public class URL {
        private static final String API_MONZO_COM = "https://api.monzo.com/";
        public static final String AUTH_MONZO_COM = "https://auth.monzo.com/";
        public static final String OAUTH2_TOKEN = API_MONZO_COM + "oauth2/token";
        public static final String AIS_ACCOUNTS = API_MONZO_COM + "ais/accounts";
        public static final String AIS_BALANCE = API_MONZO_COM + "ais/balance";
        public static final String AIS_TRANSACTIONS = API_MONZO_COM + "ais/transactions";
        public static final String PING_WHOAMI = API_MONZO_COM + "ping/whoami";
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
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public class RequestValue {
        public static final String CODE = "code";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public class StorageKey {
        public static final String OAUTH_TOKEN = "OAUTH_TOKEN";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String REDIRECT_URL = "redirect_url";
    }

}
