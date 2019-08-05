package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;

public abstract class BecConstants {

    public static final String INTEGRATION_NAME = "bec";

    public static final TypeMapper<PaymentType> PAYMENT_TYPE_MAPPER =
            TypeMapper.<PaymentType>builder()
                    .put(
                            PaymentType.DOMESTIC,
                            PaymentTypes.DANISH_DOMESTIC_CREDIT_TRANSFER,
                            PaymentTypes.INSTANT_DANISH_DOMESTIC_CREDIT_TRANSFER,
                            PaymentTypes.INTRADAY_DANISH_DOMESTIC_CREDIT_TRANSFER)
                    .build();

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder()
                    .put(PaymentStatus.PENDING, "PNDG", "RCVD")
                    .put(PaymentStatus.CANCELLED, "CANC")
                    .put(PaymentStatus.REJECTED, "RJCT")
                    .put(PaymentStatus.SIGNED, "ACCP")
                    .build();

    public static Map<String, TransactionalAccountType> accountTypes =
            new HashMap<String, TransactionalAccountType>() {
                {
                    put("aldersopsparing", TransactionalAccountType.SAVINGS);
                    put("basal indlånskonto", TransactionalAccountType.CHECKING);
                    put("børneopsparing", TransactionalAccountType.SAVINGS);
                    put("konto personale", TransactionalAccountType.CHECKING);
                    put("personalekonto", TransactionalAccountType.CHECKING);
                    put("young money", TransactionalAccountType.CHECKING);
                    put("spar nord stjernekonto", TransactionalAccountType.SAVINGS);
                    put("stjernekonto", TransactionalAccountType.CHECKING);
                    put("ung konto", TransactionalAccountType.CHECKING);
                    put("konto", TransactionalAccountType.CHECKING);
                    put("18-27 konto", TransactionalAccountType.CHECKING);
                    put("lønkonto", TransactionalAccountType.CHECKING);
                    put("al-flex-start", TransactionalAccountType.SAVINGS);
                    put("opsparingskonto", TransactionalAccountType.SAVINGS);
                    put("uddannelseskonto", TransactionalAccountType.SAVINGS);
                    put("jackpot", TransactionalAccountType.SAVINGS);
                    put("boligopsparing", TransactionalAccountType.SAVINGS);
                    put("superløn", TransactionalAccountType.CHECKING);
                    put("superløn basis", TransactionalAccountType.CHECKING);
                    put("al-børne plus", TransactionalAccountType.SAVINGS);
                    put("lommepengekonto", TransactionalAccountType.CHECKING);
                    put("al-formueflex", TransactionalAccountType.SAVINGS);
                    put("indlån", TransactionalAccountType.SAVINGS);
                    put("spar nord studiekonto", TransactionalAccountType.SAVINGS);
                    put("ungdomsopsparing", TransactionalAccountType.SAVINGS);
                    put("vestjyskungosparing", TransactionalAccountType.SAVINGS);
                    put("konfirmandkonto", TransactionalAccountType.SAVINGS);
                    put("opsparing", TransactionalAccountType.SAVINGS);
                    put("fynske teen", TransactionalAccountType.CHECKING);
                    put("børnebørnskonto", TransactionalAccountType.SAVINGS);
                    put("opsparingsinvest personale", TransactionalAccountType.SAVINGS);
                    put("Spar'Op", TransactionalAccountType.SAVINGS);
                    put("Coop Budget", TransactionalAccountType.SAVINGS);
                    put("Anfordring med AL-MC Cash", TransactionalAccountType.CHECKING);
                    put("Basal Betaling", TransactionalAccountType.CHECKING);
                    put("PrivatLøn", TransactionalAccountType.CHECKING);
                    put("Appaløn", TransactionalAccountType.CHECKING);
                    put("Totalløn", TransactionalAccountType.CHECKING);
                }
            };

    private static Map<TransactionalAccountType, List<String>> accountTypeMap =
            new HashMap<TransactionalAccountType, List<String>>() {
                {
                    put(
                            TransactionalAccountType.CHECKING,
                            accountTypes.entrySet().stream()
                                    .filter(
                                            entry ->
                                                    entry.getValue()
                                                                    .compareTo(
                                                                            TransactionalAccountType
                                                                                    .CHECKING)
                                                            == 0)
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList()));
                    put(
                            TransactionalAccountType.SAVINGS,
                            accountTypes.entrySet().stream()
                                    .filter(
                                            entry ->
                                                    entry.getValue()
                                                                    .compareTo(
                                                                            TransactionalAccountType
                                                                                    .SAVINGS)
                                                            == 0)
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList()));
                }
            };

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .putAll(accountTypeMap)
                    .put(TransactionalAccountType.SAVINGS, "SVGS", "LLSV")
                    .setDefaultTranslationValue(TransactionalAccountType.CHECKING)
                    .build();

    public static class ApiService {
        public static final String GET_CONSENT = "/consents";
        public static final String GET_CONSENT_STATUS = "/consents/{consentId}/status";
        public static final String GET_ACCOUNTS = "/accounts";
        public static final String GET_TRANSACTIONS = "/accounts/{accountId}/transactions";
        public static final String CREATE_PAYMENT = "/payments/{paymentType}";
        public static final String GET_PAYMENT = "/payments/{paymentId}";
        public static final String GET_BALANCES = "/accounts/{accountId}/balances";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String CONSENT_ID = "consentId";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
    }

    public static class QueryValues {
        public static final String TRUE = "true";
        public static final String BOTH = "both";
        public static final String BOOKED = "booked";
    }

    public static class HeaderKeys {
        public static final String ACCEPT = "accept";
        public static final String PSU_IP = "psu-ip";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String DIGEST = "digest";
        public static final String SIGNATURE = "signature";
        public static final String TPP_SIGNATURE_CERTIFICATE = "tpp-signature-certificate";
        public static final String DATE = "date";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
        public static final String CONSENT_ID = "consent-id";
    }

    public static class FormValues {
        public static final String ACCESS_TYPE = "allAccounts";
        public static final Integer FREQUENCY_PER_DAY = 4;
        public static final String TRUE = "true";
        public static final String VALID_UNTIL = "2019-09-10";
        public static final String FALSE = "false";
        public static final String EMPTY_STRING = "";
        public static final String VALID = "valid";

    }

    public static class HeaderValues {
        public static final String PSU_IP = "34.240.159.190";
        public static final String SHA_256 = "SHA-256=";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class PaymentTypes {
        public static final String INSTANT_DANISH_DOMESTIC_CREDIT_TRANSFER =
                "instant-danish-domestic-credit-transfers";
        public static final String INTRADAY_DANISH_DOMESTIC_CREDIT_TRANSFER =
                "intraday-danish-domestic-credit-transfers";
        public static final String DANISH_DOMESTIC_CREDIT_TRANSFER =
                "danish-domestic-credit-transfers";
    }

    enum HEADERS_TO_SIGN {
        X_REQUEST_ID("x-request-id"),
        TPP_REDIRECT_URI("tpp-redirect-uri"),
        DIGEST("digest");
        private String header;

        HEADERS_TO_SIGN(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }

    public class BalanceKeys {

        public static final String FORWARD_AVAILALBLE = "forwardAvailable";
    }

    public enum BALANCE_TYPE {
        FORWARD_AVAILABLE("forwardAvailable"),
        CLOSING_BOOKED("closingBooked"),
        EXPECTED("expected"),
        AUTHORIZED("authorised"),
        OPENING_BOOKED("openingBooked"),
        INTERIM_AVAILABLE("interimAvailable");

        private String type;

        BALANCE_TYPE(String type) {
            this.type = type;
        }

        public static BALANCE_TYPE fromString(String text) {
            for (BALANCE_TYPE b : BALANCE_TYPE.values()) {
                if (b.type.equalsIgnoreCase(text)) {
                    return b;
                }
            }
            return null;
        }
    }
}
