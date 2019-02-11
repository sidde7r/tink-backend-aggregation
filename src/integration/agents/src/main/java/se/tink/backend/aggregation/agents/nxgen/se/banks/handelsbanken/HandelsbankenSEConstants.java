package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.backend.agents.rpc.AccountTypes;

public class HandelsbankenSEConstants {

    public static final class QueryParams{
        public static final String IS_CARD = "isCard";
        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String AUTH_TOKEN = "authToken";

        public static final class Defaults{
            public static final String FALSE = "false";
        }
    }

    public static final class Urls {
        public static final URL ENTRY_POINT = new URL("https://m.handelsbanken.se/open/entrypoint/priv");
    }

    public static final class DeviceAuthentication {
        public static final String APP_ID = "jlAMGMG0N1YxvFvR+arooau3G+jETykFOJU16lgUOFU=";
        public static final String AUTH_TP = "1";
        public static final String VALID_SIGNATURE_RESULT = "AUTHENTICATED";
    }

    public static final class BankIdAuthentication {
        public static final String UNKNOWN_BANKID = "102";
        public static final String BANKID_UNAUTHORIZED = "104";
        public static final String TIMEOUT = "110";
        public static final String CANCELLED = "111";
        public static final String MUST_ACTIVATE = "MUST_ACTIVATE";
        public static final String DONE = "AUTHENTICATED";
    }

    public static final class BankIdUserMessages {
        public static final LocalizableKey ACTIVATION_NEEDED =
                new LocalizableKey("You need to activate your BankID in the Handelsbanken app.");
    }

    public static final class Authentication {
        public static final LogTag SE_LOGIN_REFACTORING = LogTag.from("shb_login_refactoring");
    }

    public static final class Headers {
        public static final String APP_VERSION = "3.3:8.8";
    }

    public static final class Fetcher {

        public static final ImmutableSet<String> PROVIDERS_WITH_INVERTED_TRANSACTIONS = ImmutableSet.of(
                "H", // BUSINESS_VISA
                "I" // // BUSINESS_PRIVATE_VISA
        );

        public static final String ACCOUNT_TYPE_NAME_LABEL = "kontoform";
        public static final String CREDIT_CARD_IGNORE_TYPE = "A";

        public static final class Transactions {
            public static final Pattern PENDING_PATTERN = Pattern.compile("^prel\\.?(\\s)?", Pattern.CASE_INSENSITIVE);
        }

        public static final class Loans {

            public static final String FLOATING = "rörligt";
            public static final int FLOATING_REEVALUATION_PERIOD = 3; // In months
            public static final Pattern PERIOD_PATTERN = Pattern.compile("(?<length>\\d)+\\s(?<period>[^\\s\\d]+)");
            public static final String PERIOD = "period";
            public static final String LENGTH = "length";
            public static final String YEAR = "år";
            public static final String LOAN_INFORMATION = "låneinformation";
            public static final String TERMS_OF_CHANGE = "villkorsändringsdag";
            public static final String AMORTIZATION = "amortering";
            public static final String MULTIPLE_APPLICANTS = "fler låntagare finns";
            public static final String NO_AMORTIZATION = "amorteringsfritt";
            public static final String YES = "ja";
            public static final LogTag LOG_TAG = LogTag.from("se_handelsbanken_loans");
        }

        public static class Investments {
            public static final String ERROR_TOO_YOUNG_INVESTMENTS = "10001";
            public enum InstrumentType {
                STOCK("stock", Instrument.Type.STOCK),
                FUND("fund", Instrument.Type.FUND),
                OTHER("", Instrument.Type.OTHER)
                ;
                private final String rawType;
                private final Instrument.Type type;

                InstrumentType(String rawType, Instrument.Type type) {
                    this.rawType = rawType;
                    this.type = type;
                }

                public static Instrument.Type asType(String type) {
                    for (InstrumentType instrumentType : values()) {
                        if (instrumentType.rawType.equalsIgnoreCase(type)) {
                            return instrumentType.type;
                        }
                    }
                    return OTHER.type;
                }
            }

            public enum PortfolioType {
                ISK("isk", Portfolio.Type.ISK),
                NORMAL("normal", Portfolio.Type.DEPOT),
                OTHER("", Portfolio.Type.OTHER);

                private final String rawType;
                private final Portfolio.Type type;

                PortfolioType(String rawType, Portfolio.Type type) {
                    this.rawType = rawType;
                    this.type = type;
                }

                public static Portfolio.Type asType(String type) {
                    for (PortfolioType portfolioType : values()) {
                        if (portfolioType.rawType.equalsIgnoreCase(type)) {
                            return portfolioType.type;
                        }
                    }
                    return OTHER.type;
                }
            }
        }

        public static class Transfers {
            public static final Predicate<String> PATTERN_BG_RECIPIENT = Pattern.compile(".*\\d{3,4}-\\d{4}").asPredicate();
            public static final Predicate<String> PATTERN_PG_RECIPIENT = Pattern.compile(".*\\d{1,7}-\\d").asPredicate();
            public static final LogTag LOG_TAG = LogTag.from("#se_handelsbanken_payment_context");
            public static final String UNDER_16 = "10573";
        }

        public static class Accounts {
            public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("unknown_account_type");

            public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER = TypeMapper.<AccountTypes>builder()
                    .put(AccountTypes.SAVINGS,
                            "sparkonto",
                            "e-kapitalkonto",
                            "framtidskonto",
                            "placeringskonto privat")
                    .put(AccountTypes.CHECKING, "allkonto")
                    .build();
        }
    }

    public static class Executor {
        public enum ExceptionMessages implements ExecutorExceptionResolver.ExceptionMessage {
            TRANSFER_AMOUNT_TOO_SMALL("Transfer amount is too small"),
            SOURCE_ACCOUNT_NOT_FOUND("Source account not found"),
            INVALID_DESTINATION_ACCOUNT("Destination account is not valid");

            private final String userMessage;

            ExceptionMessages(String userMessage) {
                this.userMessage = userMessage;
            }

            @Override
            public SignableOperationStatuses getStatus() {
                return SignableOperationStatuses.FAILED;
            }

            @Override
            public LocalizableKey getEndUserMessage() {
                return new LocalizableKey(userMessage);
            }

            @Override
            public LocalizableKey getUserMessage() {
                return new LocalizableKey(userMessage);
            }
        }
    }
}
