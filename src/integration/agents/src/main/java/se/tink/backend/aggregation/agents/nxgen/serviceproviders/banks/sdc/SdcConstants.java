package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SdcConstants {

    // set timeout to 5 minutes
    public static final int HTTP_TIMEOUT = 1000 * 60 * 5;
    public static final String BANK_CODE_SPARBANKEN_SYD = "9570";

    public static final LogTag HTTP_RESPONSE_LOGGER = LogTag.from("http_logging_filter");

    // after more tests we may find more errors during logon
    public enum ErrorMessage {
        LOGIN_DENIED("login denied", false),
        INCORRECT_USER_PASSWORD("incorrect user id or password", false),
        PASSWORD_EXPIRED("the password has expired", false),
        PIN_OR_CODE_NOT_VALID("pin or code is not valid", false),
        PASSWORD_BLOCKED("the password is blocked", true),
        NO_AGREEMENT("you have no agreement", false),
        DEVICE_REGISTRATION_NOT_ALLOWED("your bank doesn't allow device registration", false),
        CUSTOMER_KNOWLEDGE_QUESTIONNAIRE_PENDING("ks01052f", true),
        PIN_BLOCKED(
                "Your PIN code is blocked. You can create a new PIN in the netbank or contact your bank.",
                false),
        PIN_4_CHARACTERS(
                "{\n  \"pin\" : \"The string must contain exactly 4 characters\"\n}", false);

        private final String message;
        private final boolean blocked;

        ErrorMessage(String message, boolean blocked) {
            this.message = message;
            this.blocked = blocked;
        }

        public String getCriteria() {
            return this.message;
        }

        public static boolean isNotCustomer(String msg) {
            return msg.toLowerCase().contains(NO_AGREEMENT.message);
        }

        public static boolean isDeviceRegistrationNotAllowed(String msg) {
            return msg.toLowerCase()
                    .contains(DEVICE_REGISTRATION_NOT_ALLOWED.message.toLowerCase());
        }

        public static boolean isPinInvalid(String msg) {
            return msg.toLowerCase().contains(PIN_OR_CODE_NOT_VALID.message);
        }

        public boolean isLoginError(String msg) {
            return msg.toLowerCase().contains(this.message);
        }

        public boolean isBlocked(String msg) {
            return msg.toLowerCase().contains(this.message) && this.blocked;
        }
    }

    public static class Url {
        // initiate session, get cookies
        public static final String INIT_SESSION_PATH = "launch/launch";

        // pin login service NO, DK
        public static final String LOGON_PIN_PATH = "logon/logonpin";
        // get challenge for enabling device
        public static final String CHALLENGE_PATH = "logon/challenge";
        // pin (enable) device, i.e. create device token
        public static final String PIN_DEVICE_PATH = "devices/pin";
        public static final String SEND_OTP_REQUEST_PATH = "signing/sendotp";
        public static final String SIGN_OTP_PATH = "signing/signotp";

        // SE logon services
        public static final String BANK_ID_LOGIN_PATH_SE = "logon/sebankid";
        public static final String AGREEMENTS_PATH_SE = "logon/seagreements";
        // common services for session management
        public static final String SELECT_AGREEMENT_PATH = "logon/selectagreement";
        public static final String LOGOUT_PATH = "logon/logout";
        // common fetcher services
        public static final String FILTER_ACCOUNTS_PATH = "accounts/list/filter";
        public static final String SEARCH_TRANSACTIONS_PATH = "accounts/transactions/search";
        // investment fetcher services
        public static final String INVESTMENT_DEPOSITS_OVERVIEW_PATH =
                "investment/deposits/overview";
        public static final String INVESTMENT_DEPOSITS_CONTENT_PATH = "investment/deposits/";
        // loans
        public static final String LOAN_LIST_PATH = "loans/all/list";
        // credit cards
        public static final String CREDIT_CARD_LIST_PATH = "creditcard/list";
        public static final String CREDIT_CARD_TRANSACTIONS_PATH =
                "creditcardprovider/transactions/search";
        public static final String CREDIT_CARD_PROVIDER_ACCOUNTS_LIST_PATH =
                "creditcardprovider/account/list";
    }

    public static class Session {
        public static final String APP_VERSION = "6.8.0";
        public static final String LANGUAGE = "en";
        public static final String PLATFORM = "Android";
        public static final String PLATFORM_VERSION = "7.0";
        public static final String RESOLUTION = "768x1184";
        public static final String SCALE = "1";

        // fields for device id
        public static final String MODEL =
                "Galaxy S7"; // The end-user-visible name for the end product.
        public static final String MANUFACTURER =
                "Samsung"; // The manufacturer of the product/hardware.
        public static final String DEVICE =
                "Samsung Galaxy S7"; // The name of the industrial design.

        // session storage name for agreements during refresh
        public static final String AGREEMENTS = "AGREEMENTS";
        // session storage name for current agreement during refresh
        public static final String CURRENT_AGREEMENT = "CURRENT_AGREEMENT";

        public static final LogTag LOGIN = LogTag.from("#sdc_session_login");
        public static final String INVALID_LOGIN_MESSAGE =
                "Your bank has responded with the following message '%s'";
    }

    public static class Fetcher {

        public static final ImmutableList<String> CREDIT_CARD_NAME_TOKENS =
                ImmutableList.of("kredittkort", "kreditkort");

        public static class Investment {
            public static final LogTag INSTRUMENTS = LogTag.from("#sdc_investment_instrument");
            public static final LogTag CASH_BALANCE = LogTag.from("#sdc_investment_cashbalance");

            public enum PortfolioTypes {
                PENSION(Portfolio.Type.PENSION),
                DEPOT(Portfolio.Type.DEPOT);

                private final Portfolio.Type type;

                PortfolioTypes(Portfolio.Type type) {
                    this.type = type;
                }

                public static Optional<Portfolio.Type> parse(String rawType) {
                    return Optional.ofNullable(rawType).flatMap(PortfolioTypes::asPortfolioType);
                }

                private static Optional<Portfolio.Type> asPortfolioType(String rawType) {
                    return Arrays.stream(values())
                            .filter(t -> rawType.toUpperCase().contains(t.name()))
                            .map(t -> t.type)
                            .findFirst();
                }
            }

            // may be BOND, FUND, or SHARE. According to app.
            public enum InstrumentTypes {
                BOND(Instrument.Type.OTHER),
                FUND(Instrument.Type.FUND),
                SHARE(Instrument.Type.STOCK);

                private final Instrument.Type type;

                InstrumentTypes(Instrument.Type type) {
                    this.type = type;
                }

                public static Optional<Instrument.Type> parse(String rawType) {
                    return Optional.ofNullable(rawType).flatMap(InstrumentTypes::asInstrumentType);
                }

                private static Optional<Instrument.Type> asInstrumentType(String rawType) {
                    return Arrays.stream(values())
                            .filter(t -> rawType.toUpperCase().contains(t.name()))
                            .map(t -> t.type)
                            .findFirst();
                }
            }
        }
    }

    public enum AccountType {
        LOAN("LOAN", AccountTypes.LOAN),
        SAVINGS("DPAR", AccountTypes.SAVINGS),
        SAVINGS_BSU("BSUA", AccountTypes.SAVINGS),
        SAVINGS_AGE("RESV", AccountTypes.SAVINGS),
        SAVINGS_CHILD("CHDA", AccountTypes.SAVINGS),
        SAVINGS_MILLIONER("MLSA", AccountTypes.SAVINGS),
        CHECKING("CISP", AccountTypes.CHECKING),
        CHECKING_MEMA("MEMA", AccountTypes.CHECKING),
        E_DANKORT("ECRD", AccountTypes.CHECKING),
        CREDIT_CARD("KKPD", AccountTypes.CREDIT_CARD),
        PENSION_RTPA("RTPA", AccountTypes.PENSION),
        PENSION_CPPA("CPPA", AccountTypes.PENSION),
        INVESTMENT_POOL("PLIA", AccountTypes.INVESTMENT),
        INVESTMENT("INFA", AccountTypes.INVESTMENT),
        GUARANTEE_LETTER("GRIA", AccountTypes.OTHER),
        GUARANTEE_CAPITAL("GRCR", AccountTypes.OTHER),
        UNKNOWN("UNKNOWN", AccountTypes.OTHER);

        AccountType(String productType, AccountTypes tinkAccountType) {
            this.productType = productType;
            this.tinkAccountType = tinkAccountType;
        }

        private final String productType;
        private final AccountTypes tinkAccountType;

        public AccountTypes getTinkAccountType() {
            return this.tinkAccountType;
        }

        public static AccountType fromProductType(final String productType) {
            return Arrays.stream(AccountType.values())
                    .filter(accountType -> accountType.productType.equalsIgnoreCase(productType))
                    .findFirst()
                    .orElse(AccountType.UNKNOWN);
        }
    }

    public static class Headers {
        // header names
        public static final String X_SDC_PORTLET_PATH = "X-SDC-PORTLET_PATH";
        // note: this header affects the language of nemID notification on user's device
        public static final String X_SDC_LOCALE = "X-SDC-LOCALE";
        public static final String X_SDC_CLIENT_TYPE = "X-SDC-CLIENT-TYPE";
        public static final String X_SDC_API_VERSION = "X-SDC-API-VERSION";
        public static final String X_SDC_ACTION_CODE = "X-SDC-ACTION-CODE";
        public static final String X_SDC_DEVICE_TOKEN = "X-SDC-DEVICE-TOKEN";
        public static final String X_SDC_TRANS_ID = "X-SDC-TRANS-ID";
        public static final String X_SDC_SIGNING_TYPE = "X-SDC-SIGNING-TYPE";

        // header values
        public static final String LOCALE_EN_GB = "en_GB";
        public static final String PORTLET_PATH = "/smartphone";
        public static final String CLIENT_TYPE = "smartphone";
        public static final String API_VERSION_1 = "1";
        public static final String API_VERSION_2 = "2";
        // alternate api version is used for pin login
        public static final String API_VERSION_3 = "3";
        public static final String API_VERSION_5 = "5";
        // enable app required
        public static final String DEVICE_TOKEN_NEEDED = "DEVICE_TOKEN_NEEDED";
        public static final String DEVICE_TOKEN_RENEWAL_NEEDED = "DEVICE_TOKEN_RENEWAL_NEEDED";
        public static final ImmutableList<String> ACTION_CODE_TOKEN_NEEDED =
                ImmutableList.of(DEVICE_TOKEN_NEEDED, DEVICE_TOKEN_RENEWAL_NEEDED);
        // signing OTP
        public static final String SIGNING_NEEDED = "SIGNING_NEEDED";
        public static final ImmutableList<String> ACTION_CODE_SIGNING_NEEDED =
                ImmutableList.of(SIGNING_NEEDED, DEVICE_TOKEN_RENEWAL_NEEDED);
        public static final String SIGNING_TYPE = "PIN_OTP";

        // Error handling headers, mostly documentation
        // boolean value e.g. true
        public static final String X_SDC_IS_DEFAULT_ERROR_MESSAGE =
                "X-SDC-IS-DEFAULT-ERROR-MESSAGE";
        // String value e.g. Ett fel har uppstått. Prova igen senare eller kontakta banken och ange
        // felkod: -.
        // Ange vänligen användarnummer, datum och tid samt den handling du försökte utföra när
        // felet uppstod.
        public static final String X_SDC_ERROR_MESSAGE = "X-SDC-ERROR-MESSAGE";
        // boolean value e.g. false
        public static final String X_SDC_ERROR_IS_RETRY = "X-SDC-ERROR-IS-RETRY";
        // boolean value e.g. false
        public static final String X_SDC_ERROR_IS_CLEAR_CACHE = "X-SDC-ERROR-IS-CLEAR-CACHE";
    }

    public static class Jwt {
        public static final Charset UTF8 = Charsets.UTF_8;
        public static final String JWT_ALGORITHM = "RS256";
        public static final String JWT_TYPE = "JWT";
        public static final int JWT_EXPIRE_TIME = 180;
    }

    public static class Authentication {
        public static boolean isInternalError(HttpResponseException e) {
            HttpResponse response = e.getResponse();
            int statusCode = response.getStatus();
            return statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR;
        }
    }

    public static class Storage {
        public static final String OTP = "otp";
    }

    public static class TimeoutFilter {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }
}
