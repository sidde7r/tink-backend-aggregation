package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class Xs2aDevelopersConstants {
    public static final List<String> CHECKING_ACCOUNT_KEYS =
            ImmutableList.of(
                    "SAC",
                    "CACC",
                    "CASH",
                    "start2bank zichtrekening",
                    "0-Euro-Konto Vorteil",
                    "b.compact account",
                    "Current Account",
                    "comfort2bank zichtrekening",
                    "Girokonto");

    public static final List<String> SAVING_ACCOUNT_KEYS =
            ImmutableList.of("SAV", "Tagesgeldkonto", "SVGS", "ONDP");

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            CHECKING_ACCOUNT_KEYS.toArray(new String[0]))
                    .put(
                            AccountTypes.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            SAVING_ACCOUNT_KEYS.toArray(new String[0]))
                    .build();

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder()
                    .put(PaymentStatus.PENDING, "RCVD", "PDNG")
                    .put(
                            PaymentStatus.SIGNED,
                            "ACSC",
                            "ACTC",
                            "ACCP",
                            "ACSP",
                            "ACWC",
                            "ACWP",
                            "ACCC",
                            "ACFC")
                    .put(PaymentStatus.CANCELLED, "CANC")
                    .put(PaymentStatus.REJECTED, "RJCT")
                    .build();

    private Xs2aDevelopersConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String PARSING_URL = "Could not parse URL";
    }

    public static class ApiServices {

        public static final String CONSENT = "/berlingroup/v1/consents";
        public static final String CONSENT_DETAILS = "/berlingroup/v1/consents/{consentId}";
        public static final String CONSENT_STATUS = "/berlingroup/v1/consents/{consentId}/status";
        public static final String TOKEN = "/berlingroup/v1/token";
        public static final String GET_ACCOUNTS = "/berlingroup/v1/accounts";
        public static final String GET_BALANCES = "/berlingroup/v1/accounts/{accountId}/balances";
        public static final String GET_TRANSACTIONS =
                "/berlingroup/v1/accounts/{accountId}/transactions";
        public static final String CREATE_PAYMENT =
                "/berlingroup/v1/payments/sepa-credit-transfers";
        public static final String GET_PAYMENT =
                "/berlingroup/v1/payments/sepa-credit-transfers/{paymentId}";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_ID = "consent_id";
        public static final String PAYMENT_ID = "payment_id";
        public static final String PIS_TOKEN = "pis_token";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String AUTHORISATION_URL = "authorisation_url";
        public static final String SCA_APPROACH = "sca_approach";
        public static final String LINKS = "links";
    }

    public static class StorageValues {
        public static final String DECOUPLED_APPROACH = "DECOUPLED";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CODE_CHALLENGE_TYPE_M = "code_challenge_method";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String CODE = "code";
        public static final String CODE_CHALLENGE_TYPE = "S256";
        public static final String SCOPE = "AIS:";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONSENT_ID = "Consent-Id";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String PSU_IP_ADDRESS = "PSU-IP-ADDRESS";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_PREFFERED = "TPP-Redirect-Preferred";
        public static final String PSU_ID = "PSU-ID";
        public static final String PSU_ID_TYPE = "PSU-ID-TYPE";
    }

    public static class HeaderValues {
        public static final String RETAIL = "retail";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
        public static final String CONSENT_ID = "consentId";
    }

    public static class Transactions {
        public static final int ERROR_CODE_MAX_ACCESS_EXCEEDED = 429;
        public static final int ERROR_CODE_SERVICE_UNAVAILABLE = 503;
        public static final String HREF = "href";
        public static final int ERROR_CODE_CONSENT_INVALID = 401;
        public static final int DEFAULT_AMOUNT_TO_FETCH = 15;
        public static final int DEFAULT_CONSECUTIVE_EMPTY_PAGES_LIMIT = 4;
    }

    public static class SupplementalInfo {
        public static final String CONSENT_CONFIRMATION_FIELD = "consent-confirmation";
    }
}
