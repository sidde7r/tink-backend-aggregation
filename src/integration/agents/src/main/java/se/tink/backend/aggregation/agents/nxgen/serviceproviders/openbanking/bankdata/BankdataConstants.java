package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.util.TypePair;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentType;

public final class BankdataConstants {

    public static final GenericTypeMapper<PaymentType, TypePair> PAYMENT_TYPE_MAPPER =
            GenericTypeMapper.<PaymentType, TypePair>genericBuilder()
                    .put(PaymentType.DOMESTIC, new TypePair(Type.DK, Type.DK))
                    .put(PaymentType.SEPA, new TypePair(Type.IBAN, Type.IBAN))
                    .build();

    private BankdataConstants() {
        throw new AssertionError();
    }

    public static final Map<PaymentType, String> TYPE_TO_DOMAIN_MAPPER = new HashMap<>();

    static {
        TYPE_TO_DOMAIN_MAPPER.put(PaymentType.DOMESTIC, Endpoints.DOMESTIC_PAYMENT);
        TYPE_TO_DOMAIN_MAPPER.put(PaymentType.SEPA, Endpoints.SEPA_PAYMENT);
        TYPE_TO_DOMAIN_MAPPER.put(PaymentType.INTERNATIONAL, Endpoints.CROSS_BORDER_PAYMENT);
    }

    public static class Endpoints {
        public static final String AUTHORIZE = "/oauth-authorize";
        public static final String TOKEN = "/oauth-token";
        public static final String CONSENT = "/openbanking-consent/v1/consents";
        public static final String AUTHORIZE_CONSENT =
                "/openbanking-consent/v1/consents/{consentId}/authorisations";
        public static final String ACCOUNTS = "/openbanking-account/v1/accounts";
        public static final String AIS_PRODUCT = "/openbanking-account";

        public static final String PIS_PRODUCT = "/openbanking-payment";
        public static final String DOMESTIC_PAYMENT =
                PIS_PRODUCT + "/v1/payments/domestic-credit-transfers";
        public static final String SEPA_PAYMENT =
                PIS_PRODUCT + "/v1/payments/sepa-credit-transfers";
        public static final String CROSS_BORDER_PAYMENT =
                PIS_PRODUCT + "/v1/payments/cross-border-credit-transfers";
        public static final String AUTHORIZE_PAYMENT = "/{paymentId}" + "/authorisations";
        public static final String PAYMENT_ID = "/{paymentId}";
        public static final String GET_PAYMENT_STATUS = "/{paymentProduct}/{paymentId}/status";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String INITIAL_TOKEN = "INITIAL_TOKEN";
        public static final String CODE_VERIFIER = "CODE_VERIFIER";
        public static final String CONSENT_ID = "consentId";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
        public static final String STATE = "STATE";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String SCOPE = "scope";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String STATE = "state";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String SCOPE = "ais:";
        public static final String PIS_SCOPE = "pis:";
        public static final String CODE = "code";
        public static final String CODE_CHALLENGE_METHOD = "S256";
        public static final String TRUE = "true";
        public static final String BOTH = "both";
    }

    public static class HeaderKeys {
        public static final String X_API_KEY = "x-api-key";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class FormValues {
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String REFRESH_TOKEN_GRANT_TYPE = "refresh_token";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String SCOPE = "aisprepare pisprepare";
    }

    public static class IdTags {
        public static final String CONSENT_ID = "consentId";
        public static final String PAYMENT_ID = "paymentId";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
    }

    public static class ConsentRequest {
        public static final String ALL_ACCOUNTS_WITH_OWNER_NAME = "allAccountsWithOwnerName";
    }

    public static class Accounts {
        public static final String BALANCE_FORWARD_AVAILABLE = "forwardAvailable";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }

    public static class PaymentRequests {
        public static final String IDENTIFICATION = "endToEndIdentification";
    }

    public static class SIGNING_STEPS {
        public static final String CHECK_STATUS_STEP = "Checking_status_step";
    }

    public static class LogTags {
        public static final LogTag ERROR_FETCHING_BALANCE =
                LogTag.from("BANKDATA_ERROR_FETCHING_BALANCE");
    }

    public static class Timezone {
        public static final String UTC = "UTC";
    }
}
