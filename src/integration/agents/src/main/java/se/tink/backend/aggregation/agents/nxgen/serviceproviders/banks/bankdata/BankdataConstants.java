package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public class BankdataConstants {
    public static final String MARKET_CURRENCY = "dkk";

    public static class Url {
        private static final String BASE_URL = "https://mobil.bankdata.dk/mobilbank/";
        public static final URL LOGIN_TIME_TOKEN = new URL(BASE_URL + "login/timetoken");
        public static final URL LOGIN = new URL(BASE_URL + "login");
        public static final URL ACCOUNTS = new URL(BASE_URL + "accounts");
        public static final URL PFM_TRANSACTIONS = new URL(BASE_URL + "pfm/transactions");
        public static final URL PFM_TRANSACTIONS_FUTURE =
                new URL(BASE_URL + "pfm/transactions/future");

        public static final URL CARDS = new URL(BASE_URL + "cards");
        public static final URL INVESTMENT_POOL_ACCOUNTS =
                new URL(BASE_URL + "investment/poolaccounts");
        public static final URL DEPOSITS = new URL(BASE_URL + "deposits");
        public static final URL DEPOSITS_CONTENT_LIST = new URL(BASE_URL + "deposits/contentlist");
        public static final URL ASSET_DETAILS = new URL(BASE_URL + "asset/details");
    }

    public static class Headers {
        public static final String X_VERSION = "x-version";
        public static final String X_VERSION_VALUE = "5.17.0";
        public static final String X_BANK_NO = "x-bankNo";
        public static final String X_APPID = "x-appid";
        public static final String X_APPID_VALUE = "androidmobilbank";
    }

    public static class Authentication {
        public static final String LOGIN_SECRET =
                "3847384569284762987649827642897035873085620650398475097345093753";
        public static final String ALGORITHM = "HmacSHA256";

        public static final int ERROR_CODE_BANK_SERVICE_OFFLINE = 150;
    }

    public static class Log {
        public static final LogTag INVESTMENT_UNKNOWN_DEPOSITS_LOG_TAG =
                LogTag.from("#investment_unknown_deposits_bankdata");
        public static final LogTag INVESTMENT_UNKNOWN_POOLACCOUNT_LOG_TAG =
                LogTag.from("#investment_unknown_poolaccount_bankdata");
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
}
