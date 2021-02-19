package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.i18n.LocalizableKey;

public class HandelsbankenSEConstants {

    public static final class Currency {
        public static String SEK = "SEK";
    }

    public static final class Urls {
        public static final URL ENTRY_POINT =
                new URL("https://m.handelsbanken.se/open/entrypoint/priv");
        public static final URL HOST = new URL("https://m2.handelsbanken.se");
        public static final URL INIT_REQUEST = new URL(HOST + "/bb/gls3/aa/privmobbidse/init/3.0");
    }

    public static final class DeviceAuthentication {
        public static final String APP_ID = "UmUsM5dTKaClsKjKOYRv7o4tXQ3rn9fDxFeCB0b8BpQ=";
        public static final String AUTH_TP = "1";
        public static final String VALID_SIGNATURE_RESULT = "AUTHENTICATED";
        public static final String DEVICE_ID = "same";
    }

    public static final class BankIdAuthentication {
        public static final String UNKNOWN_BANKID = "102";
        public static final String BANKID_UNAUTHORIZED = "104";
        public static final String TIMEOUT = "110";
        public static final String CANCELLED = "111";
        public static final String FAILED_UNKNOWN = "100";
        public static final String MUST_ACTIVATE = "MUST_ACTIVATE";
        public static final String DONE = "AUTHENTICATED";
        public static final String NO_CLIENT = "NO_CLIENT_STARTED";
    }

    public static final class BankIdErrorMessages {
        public static final String FAILED_TRY_AGAIN =
                "Legitimeringen misslyckades. Var god försök igen.";
        public static final String CANCELLED = "Åtgärden avbruten.";
    }

    public static final class BankIdUserMessages {
        public static final LocalizableKey ACTIVATION_NEEDED =
                new LocalizableKey(
                        "Message from Handelsbanken - Handelsbanken needs you to verify your BankID before you can continue using the service. Visit www.handelsbanken.se or open the Handelsbanken app to verify your BankID. Note that you must be a customer of Handelsbanken to be able to use the service.");
    }

    public static final class Authentication {
        public static final LogTag SE_LOGIN_REFACTORING = LogTag.from("shb_login_refactoring");
    }

    public static final class Headers {
        public static final String APP_VERSION = "3.5:9.6";
        public static final String DEVICE_MODEL = "IOS-11.4.1,8.5.0,iPhone9.3,SEPRIV";
    }

    public static class Accounts {
        public static final String ACCOUNT_TYPE_NAME_LABEL = "kontoform";
        public static final String IBAN = "iban";
        public static final String CREDIT_CARD_IGNORE_TYPE = "A";

        public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
                TransactionalAccountTypeMapper.builder()
                        .put(
                                TransactionalAccountType.SAVINGS,
                                "sparkonto",
                                "e-kapitalkonto",
                                "framtidskonto",
                                "placeringskonto privat")
                        .put(
                                TransactionalAccountType.CHECKING,
                                AccountFlag.PSD2_PAYMENT_ACCOUNT,
                                "allkonto",
                                "allkonto ung",
                                "privatkonto",
                                "checkkonto",
                                "shb-anst kto")
                        .ignoreKeys("affärskonto", "skogskonto", "skogslikv kto")
                        .build();
    }

    public static final class Transactions {
        public static final Pattern PENDING_PATTERN =
                Pattern.compile("^prel\\.?(\\s)?", Pattern.CASE_INSENSITIVE);

        public static final ImmutableSet<String> PROVIDERS_WITH_INVERTED_TRANSACTIONS =
                ImmutableSet.of(
                        "H", // BUSINESS_VISA
                        "I" // // BUSINESS_PRIVATE_VISA
                        );
    }

    public static class Investments {
        public static final String FUND = "fund_summary";
        public static final String ISK = "isk";
        public static final String NORMAL = "normal";
        public static final String KF_AND_PENSION = "kapital";
        public static final String ERROR_TOO_YOUNG_INVESTMENTS = "10001";
        public static final String KF_TYPE_PREFIX = "kapitalspar";
        public static final String PENSION = "pension";
        public static final TypeMapper<InstrumentType> INSTRUMENT_TYPE_MAPPER =
                TypeMapper.<InstrumentType>builder()
                        .put(InstrumentType.STOCK, "stock")
                        .put(InstrumentType.FUND, "fund", "etf")
                        .build();
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

    public static final class Loans {

        public static final String BOUNDED = "bundet";
        public static final Integer BOUNDED_PERIOD = null;
        public static final String FLOATING = "rörligt";
        public static final int FLOATING_REEVALUATION_PERIOD = 3; // In months
        public static final Pattern PERIOD_PATTERN =
                Pattern.compile("(?<length>\\d)+\\s(?<period>[^\\s\\d]+)");
        public static final String PERIOD = "period";
        public static final String LENGTH = "length";
        public static final String YEAR = "år";
        public static final String LOAN_INFORMATION = "låneinformation";
        public static final String TERMS_OF_CHANGE = "villkorsändringsdag";
        public static final String AMORTIZATION = "amortering";
        public static final String MULTIPLE_APPLICANTS = "fler låntagare finns";
        public static final String NO_AMORTIZATION = "amorteringsfritt";
        public static final String NO_AMORTIZATION_2 = "läs mer och ansök";
        public static final String YES = "ja";
        public static final LogTag LOG_TAG = LogTag.from("se_handelsbanken_loans");
    }

    public static class Transfers {
        public static final Predicate<String> PATTERN_BG_RECIPIENT =
                Pattern.compile(".*\\d{3,4}-\\d{4}").asPredicate();
        public static final Predicate<String> PATTERN_PG_RECIPIENT =
                Pattern.compile(".*\\d{1,7}-\\d").asPredicate();
        public static final LogTag LOG_TAG = LogTag.from("#se_handelsbanken_payment_context");
        public static final String UNDER_16 = "10573";
        public static final String INVALID_DESTINATION_ACCOUNT = "2024";
        public static final String BANKID_SIGN_NEEDED = "9";
        public static final int BANKID_MAX_ATTEMPTS = 90;

        public static class Statuses {
            public static final List<String> TRANSFER_APPROVAL_STATUSES =
                    Arrays.asList("OK", "E-fakturan är ändrad");

            public static final String SIGN_CONFIRMED = "SIGN_CONFIRMED";
            public static final String CONTINUE = "CONTINUE";
            public static final String CANCELLED = "101";
        }
    }

    public static class ErrorMessage {
        public static final String ERROR_OCCURRED_TRY_AGAIN_LATER =
                "Ett fel har tyvärr inträffat. Försök igen senare.";
        public static final String CANNOT_ANSWER =
                "Vi kan tyvärr inte lämna svar just nu.\\nVi beklagar detta och jobbar på att lösa problemet så fort vi kan.\\nVi ber dig därför att göra ett nytt försök lite senare.";
        public static final String SERVICE_NOT_AVAILABLE = "Tjänsten kan inte nås för tillfället.";
    }

    public static class ResponseCodes {
        public static final String SERVICE_NOT_AVAILABLE = "666";
    }

    public static class Executor {
        public enum ExceptionMessages implements ExecutorExceptionResolver.ExceptionMessage {
            TRANSFER_AMOUNT_TOO_SMALL("Transfer amount is too small"),
            SOURCE_ACCOUNT_NOT_FOUND("Source account not found");

            private final String userMessage;

            ExceptionMessages(String userMessage) {
                this.userMessage = userMessage;
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

    public static class AccountPayloadKeys {
        public static final String FUND_ACCOUNT_NUMBER = "fundAccountNumbers";
    }
}
