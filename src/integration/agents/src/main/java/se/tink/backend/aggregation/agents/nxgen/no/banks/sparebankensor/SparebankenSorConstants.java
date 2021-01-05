package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;
import se.tink.libraries.i18n.LocalizableEnum;
import se.tink.libraries.i18n.LocalizableKey;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SparebankenSorConstants {
    public static final DeviceProfile DEVICE_PROFILE = DeviceProfileConfiguration.IOS_STABLE;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Url {
        static final String HOST = "https://nettbank.sor.no";
        private static final String SECESB_REST_PATH = HOST + "/secesb/rest";
        private static final String AUTHENTICATE_LOGIN_PATH = HOST + "/authenticate/login";
        public static final String BASE_PATH = SECESB_REST_PATH + "/era";

        public static final URL APP_INFORMATION =
                new URL(HOST + "/smbmobile/" + StaticUrlValues.ORG_ID + "/appversion_ios.json");
        public static final URL CONFIGURE_BANKID =
                new URL(AUTHENTICATE_LOGIN_PATH + "/bankidmobile");
        static final URL LOGIN_FIRST_STEP = new URL(SECESB_REST_PATH + "/esb/v1/login");
        static final URL LOGIN_SECOND_STEP = new URL(SECESB_REST_PATH + "/era/login");
        static final URL SEND_SMS = new URL(SECESB_REST_PATH + "/era/sam/sms");
        public static final URL FETCH_ACCOUNTS = new URL(SECESB_REST_PATH + "/era/accounts");
        public static final URL FETCH_CREDIT_CARDS =
                new URL(SECESB_REST_PATH + "/era/creditcardaccounts");

        public static final String SECESB_IDENTIFY_CUSTOMER =
                SECESB_REST_PATH + "/era/era/public/customers/";
        public static final String BANKID_MOBILE =
                AUTHENTICATE_LOGIN_PATH + "/bankidmobile;jsessionid=";
        public static final String POLL_BANKID =
                AUTHENTICATE_LOGIN_PATH + "/rest/bankidmobilestatus.json;jsessionid=";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class UrlQueryParameters {
        public static final String USER_ID = "userId";
        public static final String PHONE_NUMBER = "phoneNumber";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class StaticUrlValues {
        public static final String ORG_ID = "2811";
        public static final String E1S1 = "e1s1";
        public static final String E1S2 = "e1s2";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BankIdStatus {
        public static final String NONE = "none";
        public static final String COMPLETED = "complete";
        public static final String ERROR = "error";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LogTags {
        public static final LogTag BANKID_LOG_TAG = LogTag.from("#SparebankenSor_bankId");
        public static final LogTag CREDIT_CARD_LOG_TAG = LogTag.from("#SparebankenSor_creditcards");
        public static final LogTag LOAN_LOG_TAG = LogTag.from("#SparebankenSor_loans");
        public static final LogTag LOAN_DETAILS = LogTag.from("#SparebankenSor_loan_details");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Accounts {
        public static final String CHECKING_ACCOUNT = "spending";
        public static final String SAVINGS_ACCOUNT = "saving";
        public static final String YOUNG_PEOPLE_SAVINGS = "bsu";
        public static final String OTHER_ACCOUNT = "other";
        public static final String UNKNOWN = "unknown";
        public static final String LOAN = "loan";
        public static final String ESTATE_CREDIT = "estatecredit";

        public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
                AccountTypeMapper.builder()
                        .put(AccountTypes.SAVINGS, SAVINGS_ACCOUNT, YOUNG_PEOPLE_SAVINGS)
                        .put(AccountTypes.CHECKING, CHECKING_ACCOUNT, OTHER_ACCOUNT, UNKNOWN)
                        .put(AccountTypes.MORTGAGE, ESTATE_CREDIT)
                        .put(AccountTypes.LOAN, LOAN)
                        .build();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Storage {
        public static final String ACCESS_TOKEN = "accessToken";
        public static final String TRANSACTIONS = "transactions";
        public static final String TEMPORARY_STORAGE_LINKS = "links";
        public static final String TEMPORARY_STORAGE_CREDIT_CARD_LINKS = "ccLinks";
        public static final String EVRY_TOKEN = "evryToken";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Link {
        public static final String DETAILS = "details";
        public static final String TRANSACTIONS = "transactions";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HTMLTags {
        public static final String BANKID_REF_WORD = "bidm_ref-word";
        public static final String LOGIN_ERROR_CLASS = "bidmob-login-error";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorCode {
        public static final String WRONG_PHONE_NUMBER_OR_INACTIVATED_SERVICE_ERROR_CODE = "C161";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorText {
        public static final String BANKID_BLOCKED =
                "Det har dessverre oppstått en feil. Ditt mobilabonnement kan ha blitt endret eller din BankID på mobil kan være sperret. Aktiver BankID på mobil på nytt i din nettbank dersom feilen vedvarer.";
    }

    public enum UserMessage implements LocalizableEnum {
        ACTIVATION_CODE(new LocalizableKey("Activation code")),
        ACTIVATION_CODE_NOT_VALID(
                new LocalizableKey("The activation code you entered is not valid."));

        private LocalizableKey msg;

        UserMessage(LocalizableKey msg) {
            this.msg = msg;
        }

        @Override
        public LocalizableKey getKey() {
            return msg;
        }
    }
}
