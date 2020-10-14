package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CommerzbankConstants {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String TIMEZONE_CET = "CET";
    public static final String LOGIN_INFO_ENTITY = "LoginInfoEntity";

    public static class Urls {
        public static final String HOST = "https://app.commerzbank.de";
        public static final String OVERVIEW = "/app/rest/v3/financeoverview";
        public static final String TRANSACTIONS = "/app/rest/transactionoverview";
        public static final String LOGOUT = "/app/lp/v3/logout";
    }

    public static class Url {
        public static final String HOST = "https://app.commerzbank.de/";

        public static final URL LOGIN = new URL(HOST + "lp/applogin/v1");
        public static final URL INIT_SCA = new URL(HOST + "lp/approval/v1/init");
        public static final URL PREPARE_SCA = new URL(HOST + "lp/approval/v1/prepareApproval");
        public static final URL APPROVE_SCA = new URL(HOST + "lp/approval/v1/approve");
        public static final URL FINALISE_SCA = new URL(HOST + "lp/approval/v1/finish");

        public static final URL INIT_APP_REGISTRATION =
                new URL(HOST + "app/rest/v1/appregistration/start");
        public static final URL COMPLETE_APP_REGISTRATION =
                new URL(HOST + "app/rest/v1/appregistration/complete");
        public static final URL SEND_TWO_FACTOR_TOKEN = new URL(HOST + "app/rest/v1/send2fatoken");
        public static final URL APPROVE_CHALLENGE = new URL(HOST + "lp/v1/approveChallenge");
        public static final URL APP_REGISTRATION_UPDATE =
                new URL(HOST + "app/rest/v1/appregistration/update");
    }

    public static class Headers {
        public static final String CCB_CLIENT_VERSION = "CCB-Client-Version";
        public static final String USER_AGENT = "User-Agent";
        public static final String PRODUCT_TYPE = "productType";
        public static final String IDENTIFIER = "identifier";
        public static final String CREDIT_CARD_PRODUCT_TYPE = "creditcardProductType";
        public static final String CREDIT_CARD_IDENTIFIER = "creditcardIdentifier";
        public static final String PRODUCT_BRANCH = "productBranch";
    }

    public static class Values {
        public static final String MOB_BKNI_IOS = "MobBkniOS";
        public static final String APP_VERSION = "11.2.4";
        public static final String OS_VERSION = "11.2.1";

        public static final String CCB_CLIENT_VERSION =
                String.join("+", MOB_BKNI_IOS, APP_VERSION, OS_VERSION);
        public static final String OK = "OK";
        public static final String CURRENCY_VALUE = "EUR";
        public static final String AMOUNT_TYPE = "ALL";
        public static final String LOGOUT_OK = "logoutText.ok";
        public static final boolean CREATE_SESSION_TOKEN_FALSE = false;
        public static final boolean CREATE_SESSION_TOKEN_TRUE = true;
        public static final String BIOMETRIC_TOUCHID = "TOUCHID";
        public static final String TAN_REQUESTED = "TAN_REQUESTED";
        public static final String TAN_NOTACTIVE = "TAN_NOTACTIVE";
        public static final String CHALLENGE = "CHALLENGE";
    }

    public static class AppRegistration {
        public static final String DEVICE_DESCRIPTION = "iPhone";
        public static final String OS_TYPE = "0";
    }

    public static class CompleteAppRegistration {
        public static final String DESCRIPTION =
                String.join(
                        " - ",
                        Values.MOB_BKNI_IOS,
                        AppRegistration.DEVICE_DESCRIPTION,
                        "iOS " + Values.OS_VERSION);
    }

    public static class ACCOUNTS {
        public static final String SAVINGS_ACCOUNT = "Sparkonto";
    }

    public static class DisplayCategoryIndex {
        public static final int CHECKING = 1;
        public static final int SAVINGS_OR_INVESTMENT = 2;
        public static final int CREDIT = 3;
    }

    public static class Error {
        public static final String PIN_ERROR = "login.pin.error.10203";
        public static final String ACCOUNT_SESSION_ACTIVE_ERROR = "login.pin.error.10205";
        public static final String VALIDATION_EXCEPTION = "ccb.validationexception";
    }

    public static class ScaMethod {
        public static final String PUSH_PHOTO_TAN = "PUSH_PHOTO_TAN";
    }

    public static class Storage {
        public static final String APP_ID = "appId";
        public static final String KEY_PAIR = "keyPair";
    }

    public static class Tag {
        public static final LogTag CREDIT_CARD_FETCHING_ERROR =
                LogTag.from("#commerzbank_credit_card_fetching_error");
        public static final LogTag UNKNOWN_ACCOUNT_TYPE =
                LogTag.from("#commerzbank_unknown_account_type");
        public static final LogTag TRANSACTION_FETCHING_ERROR =
                LogTag.from("#commerzbank_transaction_fetching_error");
    }

    public static class TransactionDescriptions {
        public static final String ATM = "Kartenzahlung";
    }
}
