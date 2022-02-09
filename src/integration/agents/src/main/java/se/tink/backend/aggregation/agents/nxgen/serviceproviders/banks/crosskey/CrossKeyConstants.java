package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n_aggregation.LocalizableEnum;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class CrossKeyConstants {

    public static final class Url {
        public static final URL getUrl(String baseUrl, String path) {
            return new URL(baseUrl + path);
        }

        public static final String SYSTEM_STATUS_URI = "systemStatus.do";
        public static final String LOGIN_WITH_USERNAME_PASSWORD = "cam/pintanLoginStepPin.do";
        public static final String LOGIN_WITH_BANKID = "v2/bankIdAutostartLogin.do";
        public static final String COLLECT_BANKIID = "v3/bankIdAutostartCollect.do";
        public static final String GET_CONTENT = "getContent.do?language=sv";
        public static final String GET_LOGIN_PROVIDERS = "cam/getLoginProviders.do";
        public static final String CONFIRM_TAN_CODE = "cam/pintanLoginStepTan.do";
        public static final String LOGIN_WITH_TOKEN = "tokenLoginWithConversion.do";
        public static final String ADD_DEVICE = "v2/addDevice.do";
        public static final String FETCH_ACCOUNTS = "accounts.do";
        public static final String FETCH_TRANSACTIONS = "transactions.do";
        public static final String LOGOUT = "logout.do";
        public static final String KEEPALIVE = "keepAlive.do";
        public static final String FETCH_LOANDETAILS = "loanDetails.do";
        public static final String FETCH_GETCARDS = "cards/getCardsCached.do";
        public static final String FETCH_GETCARD = "v1/getCard.do";
        public static final String FETCH_CARD_TRANSACTIONS = "v1/getCardTransactions.do";
        public static final String FETCH_PORTFOLIO = "showPortfolio.do";
        public static final String FETCH_INSTRUMENT_DETAILS = "v1/security/instrumentDetails.do";
        public static final String FETCH_FUND_INFO = "v1/getFundInfo.do";
        public static final String IDENTITY_DATA = "customer.do";
    }

    public static final class AutoAuthentication {
        public static final String PASSWORD_STATUS_CHANGE = "CHANGE";
        public static final String ERR_PASSWORD_TOKEN_LOGIN_FAILED =
                "ERR_PASSWORD_TOKEN_LOGIN_FAILED";
        public static final String ERR_PASSWORD_MISSING =
                "ERR_PASSWORD_MISSING"; // happens if field is missing (i.e. null)
        public static final String ERR_PASSWORD_NOT_VALID = "ERR_PASSWORD_NOT_VALID";
        public static final String APP_VERSION = "1.8.0-iOS";
        public static final String LANGUAGE = "sv";
    }

    public static final class MultiFactorAuthentication {
        public static final int KEYCARD_PIN_LENGTH = 4;
        public static final String USER_DEVICE_NAME = "iOS / 10.2";
        public static final String DEVICE_INFO = "iPhone9,3";
        public static final String AUTOSTART_TOKEN = "autostartToken";
        public static final String NOT_AUTHORIZED_ERROR = "NOT_AUTHORIZED";
        public static final ImmutableMap<String, BankIdStatus> BANKID_ERROR_MAPPING =
                ImmutableMap.<String, BankIdStatus>builder()
                        .put("BANK_ID_START_FAILED", BankIdStatus.NO_CLIENT)
                        .put("INTERNAL_SERVER_ERROR", BankIdStatus.WAITING)
                        .put("BANK_ID_USER_CANCEL", BankIdStatus.CANCELLED)
                        .put("BANK_ID_EXPIRED_TRANSACTION", BankIdStatus.TIMEOUT)
                        // We try to collect BankId result from server too fast what results in this
                        // error
                        // So in BankId context we will treat this error as waiting for BankId
                        .put("NOT_AUTHORIZED", BankIdStatus.WAITING)
                        .build();
        public static final String MOBILE_BANK_ID = "MOBILE_BANK_ID";
    }

    public static final class Storage {
        public static final String DEVICE_ID = "deviceId";
        public static final String DEVICE_TOKEN = "deviceToken";
        public static final String JSESSION_ID = "jsessionId";
    }

    public static final class Query {
        public static final String APP_ID = "appId";
        public static final String LANGUAGE = "language";
        public static final String SHOW_HIDDEN = "showHidden";
        public static final String ACCOUNT_ID = "accountId";
        public static final String FROM_DATE = "fromdate";
        public static final String TO_DATE = "todate";
        public static final String ID = "id";
        public static final String CARD_ID = "cardId";
        public static final String FUND_CODE = "fundCode";
        public static final String LOAN_ACCOUNT_ID = "loanAccountId";

        public static final String VALUE_TRUE = "true";
    }

    public static final class Fetcher {
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

    public static final GenericTypeMapper<Portfolio.Type, Integer> PORTFOLIO_TYPES =
            GenericTypeMapper.<Portfolio.Type, Integer>genericBuilder()
                    .put(Portfolio.Type.DEPOT, 849)
                    .build();

    public static final GenericTypeMapper<LoanDetails.Type, Integer> LOAN_TYPES =
            GenericTypeMapper.<LoanDetails.Type, Integer>genericBuilder()
                    .put(LoanDetails.Type.BLANCO, 505)
                    .put(LoanDetails.Type.OTHER, 510)
                    .put(LoanDetails.Type.OTHER, 512)
                    .put(LoanDetails.Type.STUDENT, 532)
                    .put(LoanDetails.Type.OTHER, 536)
                    .put(LoanDetails.Type.OTHER, 539)
                    .build();

    public static final TypeMapper<Instrument.Type> INSTRUMENT_TYPES =
            TypeMapper.<Instrument.Type>builder()
                    .put(Instrument.Type.STOCK, Fetcher.Instrument.STOCK.toLowerCase())
                    .build();

    public enum EndUserMessage implements LocalizableEnum {
        PASSWORD_EXPIRED(
                new LocalizableKey(
                        "Your current password has expired and you have to create a new one."
                                + " You can do this in Ålandsbanken's app if you have a registered device, if not, please contact "
                                + "Ålandsbanken at: 0204 292 910."));

        private LocalizableKey userMessage;

        EndUserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
