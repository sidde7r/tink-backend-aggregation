package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SparebankenSorConstants {

    public static final class Url {
        static final String HOST = "https://nettbank.sor.no";
        private static final String SECESB_REST_PATH = HOST + "/secesb/rest";
        private static final String AUTHENTICATE_LOGIN_PATH = HOST + "/authenticate/login";
        public static final String BASE_PATH = SECESB_REST_PATH + "/era";

        public static final URL APP_INFORMATION = new URL(
                HOST + "/smbmobile/" + StaticUrlValues.ORG_ID + "/appversion_ios.json");
        public static final URL CONFIGURE_BANKID = new URL(AUTHENTICATE_LOGIN_PATH + "/bankidmobile");
        static final URL LOGIN_FIRST_STEP = new URL(SECESB_REST_PATH + "/esb/v1/login");
        static final URL LOGIN_SECOND_STEP = new URL(SECESB_REST_PATH + "/era/login");
        static final URL SEND_SMS = new URL(SECESB_REST_PATH + "/era/sam/sms");
        public static final URL FETCH_ACCOUNTS = new URL(SECESB_REST_PATH + "/era/accounts");
        public static final URL FETCH_CREDIT_CARDS = new URL(SECESB_REST_PATH + "/era/creditcardaccounts");

        public static final String SECESB_IDENTIFY_CUSTOMER = SECESB_REST_PATH + "/era/era/public/customers/";
        public static final String BANKID_MOBILE = AUTHENTICATE_LOGIN_PATH + "/bankidmobile;jsessionid=";
        public static final String POLL_BANKID = AUTHENTICATE_LOGIN_PATH + "/rest/bankidmobilestatus.json;jsessionid=";
    }

    public static final class UrlQueryParameters {
        public static final String USER_ID = "userId";
        public static final String PHONE_NUMBER = "phoneNumber";
    }

    public static final class StaticUrlValues {
        public static final String ORG_ID = "2811";
        public static final String E1S1 = "e1s1";
        public static final String E1S2 = "e1s2";
    }

    public static class Headers {
        public static final String NAME_CLIENTNAME = "X-EVRY-CLIENT-CLIENTNAME";
        public static final String VALUE_CLIENTNAME = "SMARTbankMobile";
        public static final String NAME_REQUESTID = "X-EVRY-CLIENT-REQUESTID";
        public static final String NAME_ACCESSTOKEN = "X-EVRY-CLIENT-ACCESSTOKEN";
        public static final String NAME_ORIGIN = "Origin";
        public static final String NAME_REQUESTED_WITH = "X-Requested-With";
        public static final String VALUE_REQUESTED_WITH = "XMLHttpRequest";
    }
    public enum StaticUrlValuePairs {
        CONFIG_KEY("configKey", "smbmactivate" + StaticUrlValues.ORG_ID),
        INIT_BANKID("execution", StaticUrlValues.E1S1),
        FINALIZE_BANKID("execution", StaticUrlValues.E1S2),
        TRANSACTIONS_BATCH_SIZE("number", "31"),
        RESERVED_TRANSACTIONS("include_authorizations", "true");

        private final String key;
        private final String value;

        StaticUrlValuePairs(String key, String value) {
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

    public static class BankIdStatus {
        public static final String NONE = "none";
        public static final String COMPLETED = "complete";
        public static final String ERROR = "error";
    }

    public static class LogTags {
        public static final LogTag BANKID_LOG_TAG = LogTag.from("#SparebankenSor_bankId");
        public static final LogTag CREDIT_CARD_LOG_TAG = LogTag.from("#SparebankenSor_creditcards");
        public static final LogTag LOAN_LOG_TAG = LogTag.from("#SparebankenSor_loans");
        public static final LogTag LOAN_DETAILS = LogTag.from("#SparebankenSor_loan_details");
    }

    public static class Accounts {
        public static final String CHECKING_ACCOUNT = "spending";
        public static final String SAVINGS_ACCOUNT = "saving";
        public static final String LOAN = "loan";
    }

    public static class Storage {
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String ACCOUNT_TRANSACTION_URLS = "accountTransactionUrls";
        public static final String TRANSACTIONS = "transactions";
        public static final String TEMPORARY_STORAGE_LINKS = "links";
    }

    public static class Link {
        public static final String DETAILS = "details";
    }
}
