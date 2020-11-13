package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;

import java.util.Arrays;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public abstract class BecConstants {

    public static final String INTEGRATION_NAME = "bec";

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "basal indlånskonto",
                            "konto personale",
                            "personalekonto",
                            "personalekonto",
                            "young money",
                            "stjernekonto",
                            "ung konto",
                            "konto",
                            "18-27 konto",
                            "lønkonto",
                            "superløn",
                            "superløn basis",
                            "lommepengekonto",
                            "fynske teen",
                            "Anfordring med AL-MC Cash",
                            "Basal Betaling",
                            "PrivatLøn",
                            "Appaløn",
                            "Totalløn")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "aldersopsparing",
                            "børneopsparing",
                            "spar nord stjernekonto",
                            "al-flex-start",
                            "opsparingskonto",
                            "uddannelseskonto",
                            "jackpot",
                            "boligopsparing",
                            "al-børne plus",
                            "al-formueflex",
                            "indlån",
                            "spar nord studiekonto",
                            "ungdomsopsparing",
                            "vestjyskungosparing",
                            "opsparing",
                            "konfirmandkonto",
                            "børnebørnskonto",
                            "opsparingsinvest personale",
                            "Spar'Op",
                            "Coop Budget",
                            "SVGS",
                            "LLSV")
                    .build();

    enum HeadersToSign {
        X_REQUEST_ID("x-request-id"),
        TPP_REDIRECT_URI("tpp-redirect-uri"),
        DIGEST("digest");
        private String header;

        HeadersToSign(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }
    }

    public enum BalanceType {
        FORWARD_AVAILABLE("forwardAvailable"),
        CLOSING_BOOKED("closingBooked"),
        EXPECTED("expected"),
        AUTHORIZED("authorised"),
        OPENING_BOOKED("openingBooked"),
        INTERIM_AVAILABLE("interimAvailable");

        private String type;

        BalanceType(String type) {
            this.type = type;
        }

        public static BalanceType fromString(String text) {
            return Arrays.stream(values())
                    .filter(balanceType -> balanceType.type.equalsIgnoreCase(text))
                    .findFirst()
                    .orElse(null);
        }
    }

    public static class ApiService {
        public static final String GET_CONSENT = "/consents";
        public static final String GET_CONSENT_STATUS = "/consents/{consentId}/status";
        public static final String GET_ACCOUNTS = "/accounts";
        public static final String GET_TRANSACTIONS = "/accounts/{accountId}/transactions";
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
        public static final Integer NUMBER_OF_VALID_DAYS = 90;
        public static final boolean TRUE = true;
        public static final boolean FALSE = false;
        public static final String EMPTY_STRING = "";
        public static final String VALID = "valid";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
    }

    public static class HeaderValues {
        public static final String SHA_256 = "SHA-256=";
        public static final String SIGNATURE_HEADER =
                "keyId=\"%s\",algorithm=\"rsa-sha256\",headers=\"%s\",signature=\"%s\"";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class PaymentProducts {
        public static final String DOMESTIC_CREDIT_TRANSFER = "domestic-credit-transfers";
        public static final String SEPA_CREDIT_TRANSFERS = "sepa-credit-transfers";
    }
}
