package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@UtilityClass
public class BankdataConstants {

    public static final String MARKET_CURRENCY = "dkk";
    public static final LogTag LOG_TAG = LogTag.from("[Bankdata]");

    @UtilityClass
    public static class Url {
        public static final String BASE_URL = "https://mobil.bankdata.dk/mobilbank/";

        // Auth
        public static final URL NEMID_INIT = new URL(BASE_URL + "nemid/init");
        public static final URL PORTAL =
                new URL(
                        "https://mobil.bankdata.dk/wps/portal/{"
                                + UrlParam.BRANCH_NAME
                                + "}/mobilnemid");
        public static final URL COMPLETE_ENROLL =
                new URL("https://mobil.bankdata.dk/mobilbank/nemid/complete_enroll");
        public static final URL LOGIN_WITH_INSTALL_ID =
                new URL("https://mobil.bankdata.dk/mobilbank/nemid/login_with_installid");

        // AIS
        public static final URL ACCOUNTS = new URL(BASE_URL + "accounts");
        public static final URL PFM_TRANSACTIONS = new URL(BASE_URL + "pfm/transactions");
        public static final URL PFM_TRANSACTIONS_FUTURE =
                new URL(BASE_URL + "pfm/transactions/future");
        public static final URL INVESTMENT_POOL_ACCOUNTS =
                new URL(BASE_URL + "investment/poolaccounts");
        public static final URL DEPOSITS = new URL(BASE_URL + "deposits");
        public static final URL DEPOSITS_CONTENT_LIST = new URL(BASE_URL + "deposits/contentlist");
        public static final URL ASSET_DETAILS = new URL(BASE_URL + "asset/details");
    }

    @UtilityClass
    public static class UrlParam {
        public static final String BRANCH_NAME = "BRANCH_NAME";
    }

    @UtilityClass
    public static class Headers {
        public static final String X_VERSION = "x-version";
        public static final String X_VERSION_VALUE = "5.26.5";
        public static final String X_BANK_NO = "x-bankNo";
        public static final String X_APPID = "x-appid";
        public static final String X_APPID_VALUE = "iphonemobilbank";
    }

    @UtilityClass
    public static class CookieName {
        public static final String MOBILE = "mobile";
        public static final String VP = "vp";
    }

    @UtilityClass
    public static final class Crypto {
        public static final String RSA_LABEL = "bdprodver001";

        // Found hardcoded in the Android app. Used for ...
        /// bd-server-login-public-key.pem
        //
        // MIIBCgKCAQEA2ZnVkX28NfqZsWX8Xse1SaWiDZUkscBijKeabTOt2LxWqhDERkVJ

        public static final String CERTIFICATE =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2ZnVkX28NfqZsWX8Xse1SaWiDZUkscBijKeabTOt2LxWqhDERkVJtYSglFJyDf0nVV+s41TvRNGTGYXBz4a6kvS9RpKY/KqfYl7zIIKI6cpI3IH17NMiEKvsMU6LpCpvhnb13iAknZAFwFohCYX/K18D6iBqxp2ZmqXEGQi30ncTtiVob4pdoiVo0WXEwSbC94haomW/WhMIPiFtl2tVIqmWwWLAXujjyBomUi2ZmwPEIA4MqPj6O09dnc8ArZpHBdbKdN2BFKsQOfD1Emw9bVxEi/zSLAeIGZDqr4sP0RytIm2iU8fz9cweS7gKh86V/tvxUSCyeHZtTiZo7nSTDwIDAQAB";

        public static final int IV_SIZE = 16;
        public static final int SESSION_KEY_SIZE = 32;
        public static final int RSA_KEY_SIZE = 2048;
    }

    @UtilityClass
    public static class Fetcher {
        public static final int START_PAGE = 0;
        public static final String DATE_FORMAT = "dd.MM.yyyy";
    }

    public static final ImmutableMap<Integer, Instrument.Type> INSTRUMENT_TYPES =
            ImmutableMap.<Integer, Instrument.Type>builder()
                    .put(1, Instrument.Type.STOCK) // danish stocks
                    .put(2, Instrument.Type.STOCK) // foreign stocks
                    .put(3, Instrument.Type.OTHER) // danish bonds
                    .put(4, Instrument.Type.OTHER) // foreign bonds
                    .build();

    public static final ImmutableMap<String, Portfolio.Type> POOLACCOUNT_TYPES =
            ImmutableMap.<String, Portfolio.Type>builder()
                    .put("pension", Portfolio.Type.PENSION)
                    .put("childsaving", Portfolio.Type.OTHER)
                    .build();

    @UtilityClass
    public static class TimeoutRetryFilterParams {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    @UtilityClass
    public static class StorageKeys {
        public static final String IDENTITY_DATA = "IDENTITY_DATA";

        public static final String KEY_PAIR_ID_STORAGE = "KAY_PAIR_ID_STORAGE";
        public static final String PUBLIC_KEY_STORAGE = "PUBLIC_KEY_STORAGE";
        public static final String PRIVATE_KEY_STORAGE = "PRIVATE_KEY_STORAGE";
        public static final String SESSION_KEY_STORAGE = "SESSION_KEY_STORAGE";
        public static final String IV_STORAGE = "IV_STORAGE";
    }

    @UtilityClass
    static class HttpClientParams {
        static final int CLIENT_TIMEOUT = 60 * 1000;
    }
}
