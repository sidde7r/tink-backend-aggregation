package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class RevolutConstants {

    public static final double REVOLUT_AMOUNT_DIVIDER = 100.0;

    public static final class Urls {
        public static final String HOST = "https://api.revolut.com";

        public static final URL USER_EXIST = new URL(HOST + "/user/exist");
        public static final URL SIGN_IN = new URL(HOST + "/signin");
        public static final URL VERIFICATION_OPTIONS = new URL(HOST + "/verification/options");
        public static final URL RESEND_CODE_VIA_CALL = new URL(HOST + "/verification-code/call");
        public static final URL CONFIRM_SIGN_IN = new URL(HOST + "/signin/confirm");
        public static final URL FEATURES = new URL(HOST + "/features");
        public static final URL USER_CURRENT = new URL(HOST + "/user/current");
        public static final URL WALLET = new URL(HOST + "/user/current/wallet");
        public static final URL TOPUP_ACCOUNTS = new URL(HOST + "/topup/accounts");
        public static final URL TRANSACTIONS = new URL(HOST + "/user/current/transactions/last");
        public static final URL PORTFOLIO = new URL(HOST + "/user/current/portfolio");
        public static final URL STOCK_INFO = new URL(HOST + "/config/wealth/STOCK");
        public static final URL STOCK_PRICE_OVERVIEW = new URL(HOST + "/instruments/priceOverview");
    }

    public static final class Storage {
        public static final String USER_ID = "userId";
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String DEVICE_ID = "deviceId";
        public static final String CURRENCY = "currency";
    }

    public static final class Tags {
        public static final String AUTHORIZATION_ERROR = "uk_revolut_authorization_error";
        public static final String PORTFOLIO_FETCHING_ERROR = "portfolio_fetching_error";
    }

    public static final class Headers {
        public static final String AUTHORIZATION_HEADER = "Authorization";
        public static final String DEVICE_ID_HEADER = "X-Device-Id";
        public static final String BASIC = "Basic ";
    }

    public static final class Params {
        public static final String PHONE = "phone";
        public static final String PHONES = "phones";
        public static final String COUNT = "count";
        public static final String TO = "to";
    }

    public static final class VerificationCodeChannel {
        public static final String SMS = "SMS";
        public static final String CALL = "CALL";
    }

    public enum AppAuthenticationValues {
        API_VERSION("X-Api-Version", "1"),
        APP_VERSION("X-Client-Version", "6.23.1"),
        MODEL("X-Device-Model", "iPhone8,1"),
        USER_AGENT("User-Agent", "Revolut/com.revolut.revolut 6633 (iPhone; iOS 12.4)");

        private final String key;
        private final String value;

        AppAuthenticationValues(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static final class ErrorMessage {
        public static final String ILLEGAL_CHARACTER = "Illegal character";
        public static final String TEMPORARY_ERROR = "server encountered a temporary error";
        public static final String HIKARIPOOL = "HikariPool";
    }

    public static final class Accounts {
        public static final String ACTIVE_STATE = "ACTIVE";
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.OTHER, "SAVINGS") // Revolut's Vault is not a bank account
                    .put(AccountTypes.CHECKING, "CURRENT")
                    .build();

    public static final TypeMapper<InstrumentModule.InstrumentType> INVESTMENT_TYPE_MAPPER =
            TypeMapper.<InstrumentModule.InstrumentType>builder()
                    .put(InstrumentModule.InstrumentType.STOCK, "STOCK")
                    .put(InstrumentModule.InstrumentType.FUND, "FUND")
                    .ignoreKeys("CASH")
                    .build();

    public static class Pagination {
        public static final int COUNT = 120;
    }

    public static class TransactionTypes {
        public static final String CARD_PAYMENT = "CARD_PAYMENT";
        public static final String TOP_UP = "TOPUP";
    }

    public static class TransactionState {
        public static final String FAILED = "FAILED";
        public static final String REVERTED = "REVERTED";
        public static final String DECLINED = "DECLINED";
        public static final String CANCELLED = "CANCELLED";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static final List<String> REVOLUT_SUPPORTED_CRYPTO_CURRENCIES =
            ImmutableList.of("BTC", "LTC", "ETH", "BCH", "XRP");
}
