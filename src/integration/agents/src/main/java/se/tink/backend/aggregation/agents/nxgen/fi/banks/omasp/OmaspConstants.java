package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n_aggregation.LocalizableEnum;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class OmaspConstants {

    static final String USER_AGENT =
            "iOS_OmaSp_PROD/2.2.1 Alamofire2.2.1iPhone iOS 10.3.1Scale/2.0";

    public static final class Storage {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String DEVICE_ID = "device_id";
        public static final String FULL_NAME = "full_name";
        public static final String DEVICE_TOKEN = "device_token";
    }

    public static final class LogTags {
        public static final LogTag LOG_TAG_AUTHENTICATION = LogTag.from("#omasp_authentication");
        public static final LogTag LOG_TAG_ACCOUNTS = LogTag.from("#omasp_accounts");
        public static final LogTag LOG_TAG_LOAN_DETAILS = LogTag.from("#omasp_loan_details");
    }

    public static final class Url {
        private static final String BASE = "https://gateway.omasp.fi/omasp/api";

        static final URL LOGIN = new URL(BASE + "/v2/identification");
        static final URL REGISTER_DEVICE = new URL(BASE + "/v2/authentication");
        static final URL ACCOUNTS = new URL(BASE + "/accounts");
        static final URL TRANSACTIONS = new URL(BASE + "/accounts/transactions");
        static final URL TRANSACTION_DETAILS =
                new URL(BASE + "/accounts/transactions/{transactionId}");
        static final URL CREDITCARDS = new URL(BASE + "/cards");
        static final URL CREDITCARD_DETAILS = new URL(BASE + "/cards/{cardId}");
        static final URL LOANS = new URL(BASE + "/loans");
        static final URL LOAN_DETAILS = new URL(BASE + "/loans/details");
    }

    public static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES =
            ImmutableMap.<String, AccountTypes>builder()
                    .put("käyttötili", AccountTypes.CHECKING) // "current account"
                    .put("säästötalletus", AccountTypes.SAVINGS) // "saving deposit"
                    .put(
                            "asuntosäästöpalkkiotili",
                            AccountTypes.SAVINGS) // "HOUSING SAVINGS PREMIUM ACCOUNT"
                    .put("yritystili", AccountTypes.CHECKING) // "company account"
                    .build();

    public static final ImmutableMap<String, LoanDetails.Type> LOAN_TYPES =
            ImmutableMap.<String, LoanDetails.Type>builder()
                    .put("other_loan", LoanDetails.Type.OTHER)
                    .put("student_loan", LoanDetails.Type.STUDENT)
                    .put("restructuring_loan", LoanDetails.Type.OTHER) // "debt settlement loan"
                    .put("home_loan", LoanDetails.Type.MORTGAGE)
                    .build();

    public static final class Error {
        public static final String BAD_REQUEST = "bad_request";
        public static final String LOGIN_WARNING = "login_warning";
        public static final String AUTHENTICATION_FAILED = "authentication_failed";
        public static final String SECURITY_KEY_FAILED = "security_key_failed";
        public static final String OTHER_BANK_CUSTOMER = "other_bank_customer";
    }

    public static final class ErrorMessage {
        public static final String LOGIN_BLOCKED = "sisäänkirjautuminen on estetty";
    }

    public enum UserMessage implements LocalizableEnum {
        LOGIN_BLOCKED(
                new LocalizableKey("Login is blocked. Contact your bank's customer service."));

        private LocalizableKey userMessage;

        UserMessage(LocalizableKey userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        public LocalizableKey getKey() {
            return userMessage;
        }
    }
}
