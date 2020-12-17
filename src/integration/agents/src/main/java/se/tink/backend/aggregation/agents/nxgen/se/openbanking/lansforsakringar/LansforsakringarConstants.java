package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;

public abstract class LansforsakringarConstants {

    public static final int MONTHS_TO_FETCH = 13;
    public static final int DAYS_TO_FETCH_BG = 89;
    public static final int MAX_NUM_RETRIES = 3;
    public static final int RETRY_SLEEP_MILLIS_SECONDS = 2000;

    public static final TypeMapper<PaymentStatus> PAYMENT_STATUS_MAPPER =
            TypeMapper.<PaymentStatus>builder().put(PaymentStatus.PENDING, "PDNG", "ACTC").build();

    public static final GenericTypeMapper<PaymentType, Pair<Type, Type>> PAYMENT_TYPE_MAPPER =
            GenericTypeMapper.<PaymentType, Pair<Type, Type>>genericBuilder()
                    .put(PaymentType.DOMESTIC, new Pair<>(Type.SE, Type.SE))
                    .put(PaymentType.SEPA, new Pair<>(Type.SE, Type.IBAN))
                    .build();

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.SAVINGS, "sparkonto")
                    .setDefaultTranslationValue(TransactionalAccountType.CHECKING)
                    .build();

    public static class Urls {
        public static final String BASE_API_URL = "https://api.bank.lansforsakringar.se:443";
        public static final String BASE_AUTH_URL = "https://secure397.lansforsakringar.se";

        public static final String AUTHORIZATION = BASE_AUTH_URL + Endpoints.AUTHORIZATION;
        public static final String TOKEN = BASE_AUTH_URL + Endpoints.TOKEN;

        public static final String CONSENT = BASE_API_URL + Endpoints.CONSENT;
        public static final String CONSENT_PROVIDED = BASE_API_URL + Endpoints.CONSENT_PROVIDED;
        public static final String SCA_STATUS = BASE_API_URL + Endpoints.SCA_STATUS;
        public static final String GET_ACCOUNTS = BASE_API_URL + Endpoints.GET_ACCOUNTS;
        public static final String GET_BALANCES = BASE_API_URL + Endpoints.GET_BALANCES;
        public static final String GET_TRANSACTIONS = BASE_API_URL + Endpoints.GET_TRANSACTIONS;
        public static final String CREATE_PAYMENT = BASE_API_URL + Endpoints.CREATE_PAYMENT;
        public static final String GET_PAYMENT = BASE_API_URL + Endpoints.GET_PAYMENT;
        public static final String GET_PAYMENT_STATUS = BASE_API_URL + Endpoints.GET_PAYMENT_STATUS;
        public static final String SIGN_PAYMENT = BASE_API_URL + Endpoints.SIGN_PAYMENT;
    }

    public static class Endpoints {

        public static final String AUTHORIZATION = "/as/authorization.oauth2";
        public static final String TOKEN = "/as/token.oauth2";

        public static final String CONSENT = "/openbanking/ano/v2/consents";
        public static final String CONSENT_PROVIDED =
                "/openbanking/ano/v2/consents/{consentId}/authorisations";
        public static final String SCA_STATUS =
                "/openbanking/sec/v2/consents/{consentId}/authorisations/{authorizationId}";
        public static final String GET_ACCOUNTS = "/openbanking/ais/v1/accounts";
        public static final String GET_BALANCES =
                "/openbanking/ais/v1/accounts/{accountId}/balances";
        public static final String GET_TRANSACTIONS =
                "/openbanking/ais/v1/accounts/{accountId}/transactions";
        public static final String CREATE_PAYMENT = "/openbanking/pis/v2/payments/{paymentType}";
        public static final String GET_PAYMENT = "/openbanking/pis/v3/payments/{paymentId}";
        public static final String GET_PAYMENT_STATUS =
                "/openbanking/pis/v3/payments/{paymentId}/status";
        public static final String SIGN_PAYMENT =
                "/openbanking/pis/v3/payments/{paymentId}/authorisations";
    }

    public static class StorageKeys {
        public static final String CONSENT_ID = "consentId";
        public static final String ACCOUNTS = "accounts";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CLIENT_ID = "client_id";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String AUTHORIZATION_ID = "AuthorisationID";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String RESPONSE_TYPE = "code";
    }

    public static class HeaderKeys {
        public static final String CONSENT_ID = "Consent-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String PSU_USER_AGENT = "PSU-User-Agent";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String PSU_ID_TYPE = "PSU-ID-Type";
        public static final String PSU_ID = "PSU-ID";
        public static final String TPP_EXPLICIT_AUTH_PREFERRED =
                "TPP-Explicit-Authorisation-Preferred";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String TPP_NOK_REDIRECT_URI = "TPP-NOK-Redirect-URI";
    }

    public static class HeaderValues {
        // TODO: We need to support these PSU headers in production.
        public static final String PSU_USER_AGENT = "Desktop Mode";
        public static final String PSU_ID_TYPE = "SSSN";
        public static final String NO_CACHE = "no-cache";
    }

    public static class FormKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String GRANT_TYPE = "grant_type";
        public static final String SEPA = "SEPA";
        public static final String CODE = "code";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CITY = "Berlin";
        public static final String COUNTRY = "Tyskland";
        public static final String STREET = "Strasse";
        public static final String DATE_FORMAT = "dd MM yyyy";
        public static final String AUTHORIZATION_CODE = "authorization_code";
    }

    public static class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_CREDENTIALS = "Client Credentials missing.";
        public static final String UNSUPPORTED_PAYMENT_TYPE = "Payment type is not supported.";
        public static final String MISSING_TOKEN = "Failed to retrieve access token.";
        public static final String SERVICE_BLOCKED = "Service_blocked";
        public static final String EXPIRED_AUTHORIZATION_CODE =
                "Authorization code is invalid or expired.";
    }

    public class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_TYPE = "paymentType";
        public static final String PAYMENT_ID = "paymentId";
        public static final String CONSENT_ID = "consentId";
        public static final String AUTHORIZATION_ID = "authorizationId";
    }

    public class PaymentTypes {
        public static final String DOMESTIC_CREDIT_TRANSFERS = "domestic-credit-transfers";
        public static final String CROSS_BORDER_CREDIT_TRANSFERS = "cross-border-credit-transfers";
    }

    public class BodyValues {
        public static final String EMPTY_BODY = "{}";
    }

    public class CallbackParam {
        private CallbackParam() {
            throw new IllegalStateException("Utility class");
        }

        public static final String PICKUP = "pickup";
    }

    public class ErrorCodes {
        public static final String SERVER = "server";
    }
}
