package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.utils.TimeUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.payment.enums.PaymentStatus;

public final class Xs2aDevelopersConstants {

    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "SAC",
                            "start2bank zichtrekening",
                            "0-Euro-Konto Vorteil",
                            "b.compact account",
                            "Current Account",
                            "Girokonto")
                    .put(
                            AccountTypes.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "SAV",
                            "Tagesgeldkonto")
                    .put(AccountTypes.CREDIT_CARD, "Prepaid-Kreditkarte")
                    .build();

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "RCVD", "ACSC").build();

    private Xs2aDevelopersConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String UNKNOWN_ACCOUNT_TYPE = "Unknown account type.";
        public static final String MISSING_AUTHENTICATOR = "Cannot find authenticator.";
        public static final String PARSING_URL = "Could not parse URL";
    }

    public static class ApiServices {

        public static final String POST_CONSENT = "/berlingroup/v1/consents";
        public static final String AUTHORIZE = "/public/berlingroup/authorize";
        public static final String TOKEN = "/berlingroup/v1/token";
        public static final String GET_ACCOUNTS = "/berlingroup/v1/accounts";
        public static final String GET_BALANCES = "/berlingroup/v1/accounts/{accountId}/balances";
        public static final String GET_AUTHORISATION =
                "/berlingroup/v1/consents/{accountId}/authorisations";
        public static final String GET_TRANSACTIONS =
                "/berlingroup/v1/accounts/{accountId}/transactions";
        public static final String CREATE_PAYMENT =
                "/berlingroup/v1/payments/sepa-credit-transfers";
        public static final String GET_PAYMENT =
                "/berlingroup/v1/payments/sepa-credit-transfers/{paymentId}";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consent_id";
        public static final String PAYMENT_ID = "payment_id";
        public static final String PIS_TOKEN = "pis_token";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String ACCOUNT_ID = "accountId";
    }

    public static class QueryKeys {
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CODE_CHALLENGE_TYPE = "code_challenge_type";
        public static final String CODE_CHALLENGE_TYPE_M = "code_challenge_method";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String WITH_BALANCE = "withBalance";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String CODE = "code";
        public static final String CODE_CHALLENGE_TYPE = "S256";
        public static final String PSU_IP_ADDRESS = "127.0.0.1";
        public static final String SCOPE = "AIS:";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONSENT_ID = "Consent-Id";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String PSU_IP_ADDRESS = "PSU-IP-ADDRESS";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String X_TINK_DEBUG = "X-Tink-Debug";
    }

    public static class FormKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String VALID_REQUEST = "valid_request";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String CLIENT_ID = "client_id";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final Boolean FALSE = false;
        public static final int FREQUENCY_PER_DAY = 4;
        public static final Boolean TRUE = true;

        public static final String VALID_UNTIL = TimeUtils.getDate();

        public static final String EUR = "EUR";
        public static final String ALL_ACCOUNTS = "allAccounts";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_ID = "paymentId";
    }

    public static class BalanceTypes {
        public static final ImmutableList<String> BALANCES =
                ImmutableList.of("authorised", "expected");
    }

    public static class Transactions {
        public static final int EMPTY_PAGES_RESPONSE_LIMIT = 1;
        public static final int ERROR_CODE_MAX_ACCESS_EXCEEDED = 429;
        public static final int ERROR_CODE_SERVICE_UNAVAILABLE = 503;
        public static final String HREF = "href";
        public static final int ERROR_CODE_INTERNAL_SERVER = 500;
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }
}
