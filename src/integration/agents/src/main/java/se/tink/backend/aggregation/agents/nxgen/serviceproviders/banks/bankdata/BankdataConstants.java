package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import java.nio.charset.Charset;
import java.security.interfaces.RSAPublicKey;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class BankdataConstants {
    public static final String MARKET_CURRENCY = "dkk";

    public static final Charset CHARSET = Charsets.UTF_8;

    public static class Url {
        public static final String BASE_URL = "https://mobil.bankdata.dk/mobilbank/";
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

        public static final URL NEMID_INIT = new URL(BASE_URL + "nemid/init");
        public static final URL NEMID_ENROLL = new URL(BASE_URL + "/nemid/complete_enroll");
        public static final URL NEMID_LOGIN = new URL(BASE_URL + "/nemid/login_with_installid");

        public static final URL NEMID_GET_CHALLENGE = new URL(BASE_URL + "/nemid/get_challenge");
    }

    public static class Headers {
        public static final String X_VERSION = "x-version";
        public static final String X_VERSION_VALUE = "5.25.1";
        public static final String X_BANK_NO = "x-bankNo";
        public static final String X_APPID = "x-appid";
        public static final String X_APPID_VALUE = "iphonemobilbank";
    }

    public static class Authentication {
        public static final String LOGIN_SECRET =
                "3847384569284762987649827642897035873085620650398475097345093753";
        public static final String ALGORITHM = "HmacSHA256";

        public static final int ERROR_CODE_BANK_SERVICE_OFFLINE = 150;
    }

    public static final class Crypto {
        public static final String RSA_LABEL = "bdprodver001";
        public static final RSAPublicKey PRODUCT_NEMID_KEY_2 =
                BankdataSecurityHelper.convertToPublicKey(
                        ("-----BEGIN CERTIFICATE-----\n"
                                        + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2ZnVkX28NfqZsWX8Xse1"
                                        + "SaWiDZUkscBijKeabTOt2LxWqhDERkVJtYSglFJyDf0nVV+s41TvRNGTGYXBz4a6"
                                        + "kvS9RpKY/KqfYl7zIIKI6cpI3IH17NMiEKvsMU6LpCpvhnb13iAknZAFwFohCYX/"
                                        + "K18D6iBqxp2ZmqXEGQi30ncTtiVob4pdoiVo0WXEwSbC94haomW/WhMIPiFtl2tV"
                                        + "IqmWwWLAXujjyBomUi2ZmwPEIA4MqPj6O09dnc8ArZpHBdbKdN2BFKsQOfD1Emw9"
                                        + "bVxEi/zSLAeIGZDqr4sP0RytIm2iU8fz9cweS7gKh86V/tvxUSCyeHZtTiZo7nST"
                                        + "DwIDAQAB\n"
                                        + "-----END CERTIFICATE-----")
                                .getBytes(Charsets.UTF_8));

        public static final String AES_PADDING = "XOXOXOXOXOXOXOXO";

        // Found hardcoded in the Android app. Used for ...
        /// bd-server-login-public-key.pem
        //
        // MIIBCgKCAQEA2ZnVkX28NfqZsWX8Xse1SaWiDZUkscBijKeabTOt2LxWqhDERkVJ

        public static final String CERTIFICATE =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2ZnVkX28NfqZsWX8Xse1SaWiDZUkscBijKeabTOt2LxWqhDERkVJtYSglFJyDf0nVV+s41TvRNGTGYXBz4a6kvS9RpKY/KqfYl7zIIKI6cpI3IH17NMiEKvsMU6LpCpvhnb13iAknZAFwFohCYX/K18D6iBqxp2ZmqXEGQi30ncTtiVob4pdoiVo0WXEwSbC94haomW/WhMIPiFtl2tVIqmWwWLAXujjyBomUi2ZmwPEIA4MqPj6O09dnc8ArZpHBdbKdN2BFKsQOfD1Emw9bVxEi/zSLAeIGZDqr4sP0RytIm2iU8fz9cweS7gKh86V/tvxUSCyeHZtTiZo7nSTDwIDAQAB";

        public static final String PUBLIC_KEY =
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl6AsWjUXvRSiRw5H4KRRijiEOrRq69WPQQ3z1wj8nQnwxztTNc5Yc7Z7Wwr2zIF4xGEUhSzks8iJr3BvUGKQzOqXynIer02FFCRq4VId1Pr62\\/yTW0t8Z6CNwosU2VSsm9jm5DeyvWvRo63UxgyDDa6LocK8X\\/eh0Mpo0IHCabPtTm6BjCCq5HW48O366Fb9nQLueCmN822CK+f\\/3FhaUkq4Kb1cXw\\/N3Go+2kmMyZkCMQmMIkM7qpPXpvbxGmzgEaCQMrI1kCB3AN3YweoIPylvU2L9Jp1Z9\\/JHGcQKHbKrKWml9v\\/P+TPTgYUemUXZlvxJweJXHL6pD4pbijQKqQIDAQAB";
        //        public static final String CERTIFICATE =
        //                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2ZnVkX28NfqZsWX8Xse1"
        //                        +
        // "SaWiDZUkscBijKeabTOt2LxWqhDERkVJtYSglFJyDf0nVV+s41TvRNGTGYXBz4a6"
        //                        +
        // "kvS9RpKY/KqfYl7zIIKI6cpI3IH17NMiEKvsMU6LpCpvhnb13iAknZAFwFohCYX/"
        //                        +
        // "K18D6iBqxp2ZmqXEGQi30ncTtiVob4pdoiVo0WXEwSbC94haomW/WhMIPiFtl2tV"
        //                        +
        // "IqmWwWLAXujjyBomUi2ZmwPEIA4MqPj6O09dnc8ArZpHBdbKdN2BFKsQOfD1Emw9"
        //                        +
        // "bVxEi/zSLAeIGZDqr4sP0RytIm2iU8fz9cweS7gKh86V/tvxUSCyeHZtTiZo7nST"
        //                        + "DwIDAQAB";

        // Found hardcoded in the Android app. Used for ...
        // epin-public-key.pem
        public static final String EPIN =
                "MIIBIDANBgkqhkiG9w0BAQEFAAOCAQ0AMIIBCAKCAQEAn1xYijAHf6BpgBCHoVcD\n"
                        + "UaYFeEz/I5/wC2FrwwlyWmU7ADbZm/Ex8myQluCTZlv/+SE9uA4Xgbkyst9cupGo\n"
                        + "jMh2C+iZpWpxXq7ulmQXPQZnkIoQmAVNfjToXRG32EVrkA3Wi/Io9YFgfLa9kANO\n"
                        + "oTNA0birqrnXiu5p8I2WN0tsh8ETF+s/f/1LHtOBBCrROw9GQZSLvrK7aVl/EiwE\n"
                        + "BlFyxFXfOoywEXXAMNr2vyaCvlOur5iHvfqUKTGBpKBjG5mdhIpy0p2/LOaDm4Gu\n"
                        + "QARwrdMDHKMzEr/A803IfFZhvTrO4k2CWgPeZECDl4Zfpu3IEQoG8iAr0LeQ7F0+\n"
                        + "RwIBAw==\n";
    }

    public static class Log {
        public static final LogTag INVESTMENT_UNKNOWN_DEPOSITS_LOG_TAG =
                LogTag.from("#investment_unknown_deposits_bankdata");
        public static final LogTag INVESTMENT_UNKNOWN_POOLACCOUNT_LOG_TAG =
                LogTag.from("#investment_unknown_poolaccount_bankdata");
    }

    public static class Storage {
        public static final String NEMID_INSTALL_ID = "NEMID_INSTALL_ID";
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

    public static class ErrorCodes {
        public static final int ACCOUNT_NOT_ACTIVATED_IN_ONLINE_BANK = 109;
        public static final int INCORRECT_CREDENTIALS = 112;
        public static final int ACCOUNT_BLOCKED = 110;
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
