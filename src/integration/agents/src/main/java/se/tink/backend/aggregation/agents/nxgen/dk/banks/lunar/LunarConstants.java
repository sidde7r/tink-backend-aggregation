package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import java.net.URI;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

public class LunarConstants {

    public static final String APP_VERSION = "4.29.1";
    private static final String LUNAR_BASE_URL = "https://api.prod.lunarway.com/";

    public static class Uri {
        public static final URI NEM_ID_AUTHENTICATE =
                toLunarUri("authentication/authenticate/nemid");
        public static final URI ACCOUNTS_VIEW = toLunarUri("accounts-view/accounts");
        public static final URI LOAN = toLunarUri("credit-loan/loan");
        public static final URI CREDIT_APPLICATIONS = toLunarUri("credit-engine/applications");

        private static URI toLunarUri(String endpoint) {
            return URI.create(LUNAR_BASE_URL + endpoint);
        }
    }

    public static class Url {
        public static final URL GOALS = toLunarUrl("goals/v2/goals");
        public static final URL TRANSACTIONS = toLunarUrl("transaction-view/transactions");
        public static final URL GOAL_DETAILS = toLunarUrl("goals/v2/goals/{goalId}/feed?from=0");
        public static final URL CARDS_BY_ACCOUNT =
                toLunarUrl("card-management/cards-by-account/{accountId}");
        public static final URL MEMBERS =
                toLunarUrl("account-management/accounts/{accountId}/members");
        public static final URL PORTFOLIO = toLunarUrl("invest/portfolio");
        public static final URL INSTRUMENTS = toLunarUrl("invest/instruments");
        public static final URL PORTFOLIO_PERFORMANCE_DATA =
                toLunarUrl("invest/portfolio/performancedata");

        private static URL toLunarUrl(String endpoint) {
            return URL.of(LUNAR_BASE_URL + endpoint);
        }
    }

    public static class Headers {
        public static final String USER_AGENT = "user-agent";
        public static final String DEVICE_MODEL = "x-devicemodel";
        public static final String REGION = "x-region";
        public static final String OS = "x-os";
        public static final String OS_VERSION = "x-osversion";
        public static final String DEVICE_MANUFACTURER = "x-devicemanufacturer";
        public static final String LANGUAGE = "x-language";
        public static final String ACCEPT_LANGUAGE = "accept-language";
        public static final String REQUEST_ID = "x-requestid";
        public static final String DEVICE_ID = "x-deviceid";
        public static final String ORIGIN = "x-origin";
        public static final String APP_VERSION = "x-appversion";
        public static final String ACCEPT_ENCODING = "accept-encoding";
        public static final String AUTHORIZATION = "authorization";
    }

    public static class HeaderValues {
        public static final String DEVICE_MODEL =
                DeviceProfileConfiguration.IOS_STABLE.getModelNumber();
        public static final String USER_AGENT_VALUE = "Lunar/99 CFNetwork/1121.2.2 Darwin/19.3.0";
        public static final String DK_REGION = "DK";
        public static final String DA_LANGUAGE = "da";
        private static final String EN_LANGUAGE = "en";
        public static final String DA_LANGUAGE_ACCEPT = "da-dk";
        public static final String I_OS = DeviceProfileConfiguration.IOS_STABLE.getOs();
        public static final String OS_VERSION =
                DeviceProfileConfiguration.IOS_STABLE.getOsVersion();
        public static final String DEVICE_MANUFACTURER =
                DeviceProfileConfiguration.IOS_STABLE.getMake();
        public static final String APP_ORIGIN = "App";
        public static final String ENCODING = "gzip, deflate, br";

        static String getLanguageCode(String userLocale) {
            String userLanguageCode =
                    Optional.ofNullable(StringUtils.left(userLocale, 2))
                            .orElse(HeaderValues.DA_LANGUAGE);

            return HeaderValues.EN_LANGUAGE.equalsIgnoreCase(userLanguageCode)
                    ? HeaderValues.EN_LANGUAGE
                    : HeaderValues.DA_LANGUAGE;
        }
    }

    public static class QueryParams {
        public static final String ORIGIN_GROUP_ID = "originGroupId";
        public static final String PAGE_SIZE = "pageSize";
        public static final String BEFORE_QUERY = "before";
    }

    public static class QueryParamsValues {
        public static final int PAGE_SIZE = 200;
    }

    public static class PathParams {
        public static final String GOAL_ID = "goalId";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class Storage {
        public static final String PROCESS_STATE_KEY = "LunarProcessState";
        public static final String PERSISTED_DATA_KEY = "LunarAuthData";
    }

    static class HttpClient {
        static final int MAX_RETRIES = 5;
        static final int RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
