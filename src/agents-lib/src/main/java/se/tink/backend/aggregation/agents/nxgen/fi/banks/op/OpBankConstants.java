package se.tink.backend.aggregation.agents.nxgen.fi.banks.op;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.agents.rpc.AccountTypes;

public class OpBankConstants {

    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final String LANGUAGE_CODE = "en";
    public static final String AUTH_TOKEN_PREFIX = "G";
    public static final int ONE_WEEK_AGO_IN_DAYS = -7;
    public static final int KEYCARD_PIN_LENGTH = 4;

    public static final List<String> DEFAULT_CONFIGURATIONS =
            ImmutableList.of("LightLogin", "OfflineNfs", "EasyMyTransfer");

    public static final String DEFAULT_CONFIGURATION_NAME = "Tink";

    public static final String PARAM_NAME_ACCOUNT_NUMBER = "ACCOUNT_NUMBER";
    public static final String PARAM_NAME_EXPIRY_DATE = "EXPIRY_DATE";
    public static final String PARAM_NAME_CARD_NUMBER = "CARD_NUMBER";
    public static final String QUERY_STRING_KEY_FIRST_PAGE = "firstPage";
    public static final String PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED = "AGREEMENT_NUMBER_ENCRYPTED";
    public static final String PARAM_NAME_PORTFOLIO_ID = "PORTFOLIO_ID";

    public static class Urls {

        private static final String BASE_URL = "https://mobile.op-palvelut.fi";
        public static final URL TRANSACTIONS_URL =
                new URL(BASE_URL + "/mobile/accounts/{" + PARAM_NAME_ACCOUNT_NUMBER + "}/transactions");
        public static final URL ACCOUNTS_URI = new URL(BASE_URL + "/mobile/accounts");
        public static final URL INIT_URI = new URL(BASE_URL + "/mobile/device/mob/version");
        public static final URL LOGIN_URI = new URL(BASE_URL + "/authentication/keylist/rest/login");
        public static final URL REFRESH_SESSION_URI = new URL(BASE_URL + "/mobile/refreshSession");
        public static final URL LOGOUT_URI = new URL(BASE_URL + "/authentication/common/rest/logout");
        public static final URL AUTHENTICATE_URI = new URL(BASE_URL + "/authentication/keylist/rest/authenticate");
        public static final URL CONFIGURATION_URI = new URL(BASE_URL + "/mobile/configuration/mob");
        public static final URL REPRESENTATION_TYPE = new URL(BASE_URL + "/authentication/common/rest/select-representation");
        public static final URL POSTLOGIN = new URL(BASE_URL + "/mobile/postlogin");
        public static final URL AUTH_TOKEN_CONFIG = new URL(BASE_URL + "/mobile/configuration/{identifier}/adobeAnalytics");

        //TODO: BELOW ARE NOT TESTED!! FIND THE STRUCTURES IN LOGGING
        public static final URL TRADING_ASSETS_SUMMARY = new URL(BASE_URL + "/mobile/trading/assets/summary");
        public static final URL TRADING_ASSETS_PORTFOLIOS = new URL(BASE_URL + "/mobile/trading/assets/portfolios");
        // -- Should look like this when tested/mobile/trading/assets/portfolio/" + string + "?type=all"
        public static final URL TRADING_ASSETS_PORTFOLIO_DETAILS = new URL(BASE_URL + "/mobile/trading/assets/portfolio/{" + PARAM_NAME_PORTFOLIO_ID + "}");
        public static final URL TRADING_FUNDS = new URL(BASE_URL + "/mobile/trading/funds");
        public static final URL CREDITS = new URL(BASE_URL + "/mobile/credits");

        public static final URL COLLATERAL_CREDITS_DETAILS = new URL(
                BASE_URL + "/mobile/creditdetails/{" + PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED
                       + "}/WITH_COLLATERAL");

        public static final URL FLEXI_CREDITS_DETAILS = new URL(
                BASE_URL + "/mobile/creditdetails/{" + PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED
                        + "}/FLEXI_CREDIT");

        public static final URL SPECIAL_CREDITS_DETAILS = new URL(
                BASE_URL + "/mobile/creditdetails/{" + PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED
                        + "}/SPECIAL_CREDIT");

        public static final URL CONTINUING_CREDITS_TRANSACTIONS = new URL(
                BASE_URL + "/mobile/creditdetails/{" + PARAM_NAME_AGREEMENT_NUMBER_ENCRYPTED
                       + "}/CONTINUING_CREDIT/transactions");
        public static final URL CARDS = new URL(BASE_URL + "/mobile/cards");

        public static final URL CARDS_DETAILS = new URL(
                BASE_URL + "/mobile/cards/{" + PARAM_NAME_CARD_NUMBER + "}/details/{" + PARAM_NAME_EXPIRY_DATE + "}");

        //TODO FIX THIS -- Should look like "/mobile/cards/transactions?firstPage=true
        public static final URL CARDS_TRANSACTIONS_URL = new URL(BASE_URL + "/mobile/cards/transactions");
    }

    public static class IdTags{
        public static final String IDENTIFIER_TAG = "identifier";
        public static final String AUTH_TOKEN_TAG = "authToken";
        public static final String FROM_DATE_TAG = "startdate";
        public static final String TO_DATE_TAG = "timestamp";
        public static final String APPLICATION_ID_TAG = "ApplicationId";
        public static final String REPTYPE_TAG = "REPTYPE";
        public static final String REPTYPE_PERSON_TAG = "PERSON";
    }


    public static class Init {
        public static final String OS_NAME = "ios";
        public static final String OS_VERSION = "10.3.1";
        public static final boolean ROOTED = false;
        public static final String APP_VERSION = "22.0";
        public static final String HW_TYPE = "mobile";
        public static final String CORE = "KR2";
        public static final String APPLICATION_GROUP_ID = "mob";
    }

    // KEY for AUTH_TOKEN used by iphone
    //    private static final String KEY = "f944972f20ea3de522f312d4a5baf0f9";
    public static class KeyGenerator {

        public static final String CHAR_STRING_1 =
                "3gg6gom3jtgk6skkj0i6767i6f78n31k28u9j8bg9755ac1dmr7528on4c2s4rsi9f02i2ev6moicc5f0ijbml9fb4hdumtsfqkf3hkgull6clm20rmpqr4fnuup1994";
        public static final String CHAR_STRING_2 =
                "de9it27nr0n241oa7jlif84emv56gtdd06ol0fi9h09l1jkeksar8oae1urns2pj8ljpg1b7ra7hbhak1gnbj0i2dpskf9rslfnaippvgqgfhd60ovdgp9ds914uharf";
        public static final String CHAR_STRING_3 =
                "ardcm9ih9d3tfdd12hhfeq3ohrp1c455a97s3pgae6ep15jhd15saer7v6bv7jcbghgbf4cs765qkm4it1dt0nkolrp0tpjsbd02236gieuspp4acuhf1allngof5qd0";
        public static final String CHAR_STRING_4 =
                "viktdqgdctlefcudcpo5icnacf1ji370enfq5lmnl10qr0eepoqukjgrmigoudqifc0rvpchqa2qqte0ghpkh5c15g3m1popgjhfpt6hnstbdang36s7vs7pg2bs9sds";
        public static final int RAND_INT_A = 123456789;

        public static final int RAND_INT_B = 362436069;
        public static final int RAND_INT_C = 521288629;
        public static final int RAND_INT_D = 88675123;
    }


    public static class LoginConstants{
        public static final String GROUP_ID = "mob";
        public static final String TOUCH_ID_ENABLED = "false";
    }

    public static class Authentication {
        public static final String APPLICATION_INSTANCE_ID = "applicationInstanceId";
        public static final int UNAUTHENTICATED_STATUS = 401;
        public static final String UNAUTHENTICATED_MESSAGE = "KR_false_pin";

        public static class Level {
            public static final String LOGGEDIN_WITH_LIGHTLOGIN = "VPT_LMA";
            public static final String LOGGEDIN = "VPT";
            public static final String STRONGLY_AUTHENTICATED = "STRONGLY_AUTHENTICATED";
        }
    }

    public static class RequestParameters{
        public static final String FILTER_ALL_PARAM = "filter";
        public static final String FILTER_ALL_VALUE = "all";
        public static final String OVERRIDE_PARAM = "override";
        public static final String OVERRIDE_VALUE = "true";
        public static final String CREATE_NEW_PARAM = "createNew";
        public static final String CREATE_NEW_VALUE = "true";
        public static final String START_DATE_PARAM = "startDate";
        public static final String TIMESTAMP_PARAM = "timestamp";
        public static final String TYPE_PARAM = "type";
        public static final String TYPE_VALUE = "all";
    }

    public static class TypeCode {
        public static final ImmutableMap<String, AccountTypes> ACCOUNT_TYPES_BY_TYPE_CODE =
                ImmutableMap.<String, AccountTypes>builder()
                        .put("710001", AccountTypes.CHECKING)
                        .put("710002", AccountTypes.CHECKING)

                        .put("710011", AccountTypes.OTHER)
                        .put("710012", AccountTypes.OTHER)
                        .put("710013", AccountTypes.OTHER)

                        // På ett Målkonto sparar du enkelt för kommande behov.
                        // På ett Målkonto är besparingarna tillgängliga då du behöver dem.
                        .put("711030", AccountTypes.SAVINGS)

                        .put("711037", AccountTypes.PENSION)

                        // Tillväxtränta är hushållets reservkonto som ger bättre avkastning på
                        // besparingarna. På ett Tillväxträntekonto är besparingarna tillgängliga
                        // då du behöver dem.
                        .put("712035", AccountTypes.SAVINGS)

                        // Gruppränta är ett gruppkonto, där den ränta som betalas på insättningarna
                        // växer i takt med de totala insättningarna - upp till maximiräntan.
                        .put("712050", AccountTypes.OTHER)

                        // För ett tidsbundet räntekonto fastställs en förfallodag,
                        // före vilken medlen på kontot inte får lyftas
                        .put("712007", AccountTypes.SAVINGS)

                        // På ett fortlöpande räntekonto placerar du pengar utan någon
                        // fastställd placeringstid
                        .put("712008", AccountTypes.SAVINGS)
                        .put("712015", AccountTypes.SAVINGS)

                        .put("110001", AccountTypes.OTHER)
                        .put("120000", AccountTypes.OTHER)
                        .put("120001", AccountTypes.OTHER)
                        .build();
    }

    public static final class Fetcher {
        public static final String COLLATERAL_CREDIT = "WITH_COLLATERAL";
        public static final String CONTINUING_CREDIT = "CONTINUING_CREDIT";
        public static final String FLEXI_CREDIT = "FLEXI_CREDIT";
        public static final String SPECIAL_CREDIT = "SPECIAL_CREDIT";

        public static final LogTag INVESTMENT_PORTFOLIO_TYPE_LOGGING =
                LogTag.from("#investment-portfolio-type-logging-opbank-fi");
        public static final LogTag INVESTMENT_LOGGING = LogTag.from("#investment-logging-opbank-fi");
        public static final LogTag LOAN_LOGGING = LogTag.from("#loan-logging-opbank-fi");
        public static final LogTag CREDIT_LOGGING = LogTag.from("#credit-logging-opbank-fi");
        public static final LogTag CREDITCARD_LOGGING = LogTag.from("#creditcard-logging-opbank-fi");

        public static final ImmutableList<String> KNOWN_PORTFOLIO_TYPES = ImmutableList.of("funds", "stocks", "bonds");
        public static final Pattern EXTRACT_INSTRUMENT_GROUP_PATTERN =
                Pattern.compile("\"instrumentGroup([a-z0-9]+)\"", Pattern.CASE_INSENSITIVE);
        public static final String INSTRUMENT_TYPE_BOND = "310";
    }

    public enum LoanType {
        STUDENT_LOAN("student loan", LoanDetails.Type.STUDENT),
        HOUSING_LOAN("home loan", LoanDetails.Type.MORTGAGE),
        OTHER_LOAN("", LoanDetails.Type.OTHER);

        private final String usage;
        private final LoanDetails.Type tinkType;

        LoanType(String usage, LoanDetails.Type type) {
            this.usage = usage;
            this.tinkType = type;
        }

        public LoanDetails.Type getTinkType() {
            return tinkType;
        }

        public static LoanType findLoanType(String usage) {
            return Arrays.stream(LoanType.values())
                    .filter(loanType -> loanType.usage.equalsIgnoreCase(usage)).findFirst().orElse(OTHER_LOAN);
        }

        public static boolean isHandled(String name) {
            return findLoanType(name) != OTHER_LOAN;
        }
    }
}
