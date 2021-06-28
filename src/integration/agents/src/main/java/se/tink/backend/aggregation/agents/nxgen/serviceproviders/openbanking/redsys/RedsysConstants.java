package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class RedsysConstants {

    // Some banks will fail at certain times when using 90 days
    public static final int DEFAULT_REFRESH_DAYS = 89;

    private RedsysConstants() {
        throw new AssertionError();
    }

    // partial ISO 20022 ExternalCashAccountType1Code
    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CACC",
                            "TRAN")
                    .put(TransactionalAccountType.SAVINGS, "SVGS")
                    .build();

    public static class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class Urls {
        public static final String BASE_AUTH_URL =
                "https://hubpsd2.redsys.es/api-oauth-xs2a/services/rest";
        public static final String BASE_API_URL =
                "https://psd2.redsys.es/api-entrada-xs2a/services";
        public static final String OAUTH = "/authorize";
        public static final String TOKEN = "/token";
        public static final String REFRESH = "/token";
        public static final String CONSENTS = "/v1/consents";
        public static final String CONSENT = "/v1/consents/%s";
        public static final String CONSENT_STATUS = "/v1/consents/%s/status";
        public static final String ACCOUNTS = "/v1/accounts";
        public static final String TRANSACTIONS = "/v1/accounts/%s/transactions";
        public static final String BALANCES = "/v1/accounts/%s/balances";
        public static final String CREATE_PAYMENT = "/v1/payments/%s";
        public static final String FETCH_PAYMENT = "/v1/payments/%s/%s";
        public static final String FETCH_PAYMENT_STATUS = "/v1/payments/%s/%s/status";
        public static final String CANCEL_PAYMENT = "/v1/payments/%s/%s";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_ID = "consentId";
        public static final String CONSENT_VALID_FROM = "consentValidFrom";
        public static final String FETCHED_TRANSACTIONS = "fetchedTxUntil:";
    }

    public static class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String OK = "ok";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String RESPONSE_TYPE = "code";
        public static final String SCOPE = "AIS PIS";
        public static final String AIS_SCOPE = "AIS";
        public static final String CODE_CHALLENGE_METHOD = "S256";
        public static final String TRUE = "true";
        public static final String FALSE = "false";

        public static final class BookingStatus {
            public static final String BOOKED = "booked";
            public static final String PENDING = "pending";
        }
    }

    public static class HeaderKeys {
        public static final String REQUEST_ID = "X-Request-ID";
        public static final String IBM_CLIENT_ID = "X-IBM-Client-Id";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String DIGEST = "Digest";
        public static final String SIGNATURE = "Signature";
        public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String TPP_NOK_REDIRECT_URI = "TPP-Nok-Redirect-URI";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class HeaderValues {
        public static final String TRUE = "true";
    }

    public static class FormKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ASPSP = "aspsp";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final boolean TRUE = true;
        public static final boolean FALSE = false;
        public static final int FREQUENCY_PER_DAY = 4;
        public static final String ALL_ACCOUNTS = "allAccounts";
    }

    public static class Signature {
        public static final String DIGEST_PREFIX = "SHA-256=";
        public static final String FORMAT =
                "keyId=\"%s\",algorithm=\"SHA-256\",headers=\"%s\",signature=\"%s\"";
    }

    public static class Links {
        public static final String SCA_REDIRECT = "scaRedirect";
        public static final String NEXT = "next";
    }

    public static class ErrorCodes {
        public static final String ACCESS_EXCEEDED = "ACCESS_EXCEEDED";
        public static final String CONSENT_EXPIRED = "CONSENT_EXPIRED";
        public static final String SERVER_ERROR = "server_error";
    }

    public static class HttpErrorCodes {
        public static final int TOO_MANY_REQUESTS = 429;
    }

    public static class BalanceType {
        public static final String CLOSING_BOOKED = "closingBooked";
        public static final String EXPECTED = "expected";
        public static final String OPENING_BOOKED = "openingBooked";
        public static final String INTERIM_AVAILABLE = "interimAvailable";
    }

    public static class Storage {
        public static final String PAYMENT_SCA_REDIRECT = "scaRedirect";
        public static final String STATE = "payment_state";
    }

    public static class Timer {
        public static final long WAITING_FOR_QUIT_PENDING_STATUS_MILISEC = 3000L;
    }

    public static class PaymentStep {
        public static final String IN_PROGRESS = "IN_PROGRESS";
    }
}
