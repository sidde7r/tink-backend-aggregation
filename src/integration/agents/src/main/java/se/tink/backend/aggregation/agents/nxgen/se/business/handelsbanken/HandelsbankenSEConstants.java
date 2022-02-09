package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken;

import java.util.regex.Pattern;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public class HandelsbankenSEConstants {

    public static final String CURRENCY = "SEK";

    public static final class Urls {
        public static final URL HOST = new URL("https://m2.handelsbanken.se");

        public static final URL ENTRY_POINT =
                new URL("https://m.handelsbanken.se/open/entrypoint/corp");
        public static final URL INIT_REQUEST = new URL(HOST + "/bb/gls3/aa/corpmobbidse/init/3.0");
    }

    public static final class Headers {
        public static final String APP_VERSION = "3.5:4.3";
        public static final String DEVICE_MODEL = "iOS;Apple;iPhone10.4";
    }

    public static final class DeviceAuthentication {
        public static final String APP_ID = "oFrZfGc1/M0yN4jZVSqZnpprq4N0rqnRgtPMT7onHxQ=";
        public static final String AUTH_TP = "1";
        public static final String VALID_SIGNATURE_RESULT = "AUTHENTICATED";
        public static final String SELECTION_REQUIRED = "SELECTION_REQUIRED";
        public static final String DEVICE_ID = "same";
    }

    public static final class BankIdAuthentication {
        public static final String UNKNOWN_BANKID = "102";
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
                new LocalizableKey("You need to activate your BankID in the Handelsbanken app.");
    }

    public static class ErrorMessage {
        public static final String ERROR_OCCURRED_TRY_AGAIN_LATER =
                "Ett fel har tyvärr inträffat. Försök igen senare.";
        public static final String CANNOT_ANSWER =
                "Vi kan tyvärr inte lämna svar just nu.\\nVi beklagar detta och jobbar på att lösa problemet så fort vi kan.\\nVi ber dig därför att göra ett nytt försök lite senare.";
        public static final String SERVICE_NOT_AVAILABLE = "Tjänsten kan inte nås för tillfället.";
    }

    public static class ResponseCodes {
        public static final String SERVICE_NOT_AVAILABLE = "999";
    }

    public static final class Transactions {
        public static final Pattern PENDING_PATTERN =
                Pattern.compile("^prel\\.?(\\s)?", Pattern.CASE_INSENSITIVE);
    }

    public static class Accounts {
        public static final String ACCOUNT_TYPE_NAME_LABEL = "kontoform";
        public static final String ACCOUNT_NUMBER_LABEL = "kontonummer";
        public static final String IBAN_LABEL = "iban";
        public static final String BANKGIRO_NUMBER_LABEL = "bankgironummer";

        public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
                TransactionalAccountTypeMapper.builder()
                        .put(
                                TransactionalAccountType.CHECKING,
                                AccountFlag.PSD2_PAYMENT_ACCOUNT,
                                "Affärskonto",
                                "Skogskonto",
                                "Skogslikv kto",
                                "PM-Konto")
                        .build();
    }
}
