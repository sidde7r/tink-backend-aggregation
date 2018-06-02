package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;

public class AlandsBankenConstants {

    public static final class Url {
        private static final String BASE = "https://mob.alandsbanken.fi/cbs-inet-json-api-aab-v1/api/";

        static final URL LOGIN_WITHOUT_TOKEN = new URL(BASE + "cam/pintanLoginStepPin.do");
        static final URL LOGIN_WITH_TOKEN = new URL(BASE + "tokenLoginWithConversion.do");
        static final URL CONFIRM_TAN_CODE = new URL(BASE + "cam/pintanLoginStepTan.do");
        static final URL ADD_DEVICE = new URL(BASE + "v2/addDevice.do");
        static final URL FETCH_ACCOUNTS = new URL(BASE + "accounts.do");
        static final URL FETCH_TRANSACTIONS = new URL(BASE + "transactions.do");
        static final URL LOGOUT = new URL(BASE + "logout.do");
        static final URL KEEPALIVE = new URL(BASE + "keepAlive.do");
        static final URL FETCH_LOANDETAILS = new URL(BASE + "loanDetails.do");
        static final URL FETCH_GETCARDS = new URL(BASE + "v1/getCards.do");
        static final URL FETCH_GETCARD = new URL(BASE + "v1/getCard.do");
        static final URL FETCH_CARD_TRANSACTIONS = new URL(BASE + "v1/getCardTransactions.do");
        static final URL FETCH_PORTFOLIO = new URL(BASE + "showPortfolio.do");
        static final URL FETCH_INSTRUMENT_DETAILS = new URL(BASE + "v1/security/instrumentDetails.do");
        static final URL FETCH_FUND_INFO = new URL(BASE + "v1/getFundInfo.do");
    }

    public static final class AutoAuthentication {
        public static final String ERR_PASSWORD_TOKEN_LOGIN_FAILED = "ERR_PASSWORD_TOKEN_LOGIN_FAILED";
        public static final String ERR_PASSWORD_MISSING = "ERR_PASSWORD_MISSING";  // happens if field is missing (i.e. null)
        public static final String ERR_PASSWORD_NOT_VALID = "ERR_PASSWORD_NOT_VALID";
        public static final String APP_VERSION = "1.4.0-iOS";
    }

    public static final class MultiFactorAuthentication {
        public static final String TAN = "tan";
        public static final String USER_DEVICE_NAME = "iOS / 10.2";
        public static final String DEVICE_INFO = "iPhone9,3";
        public static final String TAN_INVALID = "TAN_INVALID";
        public static final String PIN_CODE_INVALID = "PIN_CODE_INVALID";
        public static final String USER_LOCKED = "USER_LOCKED";
    }

    public static final class Storage {
        public static final String DEVICE_ID = "deviceId";
        public static final String DEVICE_TOKEN = "deviceToken";
    }

    public static final class Fetcher {
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan_logging_alandsbank_fi");
        public static final LogTag INVESTMENT_PORTFOLIO_LOGGING = LogTag.from
                ("#investment-portfolio-logging-alandsbank-fi");
        public static final LogTag INVESTMENT_INSTRUMENT_LOGGING = LogTag.from
                ("#investment-instrument-logging-alandsbank-fi");

        public static final class Account {
            public static final String CHECK = "check";
            public static final String INVESTMENT = "portfolio";
            public static final String LOAN = "loan";
            public static final String SAVING = "saving";
        }

        public static final class Instrument {
            public static final String STOCK = "STOCK";
        }
    }

    public static final ImmutableMap<Integer, Portfolio.Type> PORTFOLIO_TYPES = ImmutableMap.<Integer, Portfolio.Type>builder()
            .put(849, Portfolio.Type.DEPOT)
            .build();

    public static final ImmutableMap<Integer, LoanDetails.Type> LOAN_TYPES = ImmutableMap.<Integer, LoanDetails.Type>builder()
            .put(505, LoanDetails.Type.BLANCO)
            .put(510, LoanDetails.Type.OTHER)
            .put(512, LoanDetails.Type.OTHER)
            .put(532, LoanDetails.Type.STUDENT)
            .put(536, LoanDetails.Type.OTHER)
            .put(539, LoanDetails.Type.OTHER)
            .build();

    public static final ImmutableMap<String, Instrument.Type> INSTRUMENT_TYPES = ImmutableMap.<String, Instrument.Type>builder()
            .put(Fetcher.Instrument.STOCK.toLowerCase(), Instrument.Type.STOCK)
            .build();
}
