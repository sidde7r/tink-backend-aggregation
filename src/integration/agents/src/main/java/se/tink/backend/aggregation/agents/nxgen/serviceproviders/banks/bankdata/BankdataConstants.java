package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BankdataConstants {
    public static final String MARKET_CURRENCY = "dkk";

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

    public static class UrlParam {
        public static final String BRANCH_NAME = "BRANCH_NAME";
    }

    public static class Headers {
        public static final String X_VERSION = "x-version";
        public static final String X_VERSION_VALUE = "5.25.1";
        public static final String X_BANK_NO = "x-bankNo";
        public static final String X_APPID = "x-appid";
        public static final String X_APPID_VALUE = "iphonemobilbank";
    }

    public static class CookieName {
        public static final String MOBILE = "mobile";
        public static final String VP = "vp";
    }

    public static final class Crypto {
        public static final String RSA_LABEL = "bdprodver001";

        // Found hardcoded in the Android app. Used for ...
        /// bd-server-login-public-key.pem
        //
        // MIIBCgKCAQEA2ZnVkX28NfqZsWX8Xse1SaWiDZUkscBijKeabTOt2LxWqhDERkVJ

        public static final String CERTIFICATE =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2ZnVkX28NfqZsWX8Xse1SaWiDZUkscBijKeabTOt2LxWqhDERkVJtYSglFJyDf0nVV+s41TvRNGTGYXBz4a6kvS9RpKY/KqfYl7zIIKI6cpI3IH17NMiEKvsMU6LpCpvhnb13iAknZAFwFohCYX/K18D6iBqxp2ZmqXEGQi30ncTtiVob4pdoiVo0WXEwSbC94haomW/WhMIPiFtl2tVIqmWwWLAXujjyBomUi2ZmwPEIA4MqPj6O09dnc8ArZpHBdbKdN2BFKsQOfD1Emw9bVxEi/zSLAeIGZDqr4sP0RytIm2iU8fz9cweS7gKh86V/tvxUSCyeHZtTiZo7nSTDwIDAQAB";
    }

    public static class Fetcher {
        public static final int START_PAGE = 0;
        public static final int ITEMS_PER_PAGE = 25;
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

    public static class TimeoutRetryFilterParams {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static class StorageKeys {
        public static final String IDENTITY_DATA = "IDENTITY_DATA";
    }

    static class HttpClientParams {
        static final int CLIENT_TIMEOUT = 60 * 1000;
    }
}
