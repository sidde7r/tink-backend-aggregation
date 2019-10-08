package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.pair.Pair;
import se.tink.libraries.payment.enums.PaymentType;

public final class SantanderConstants {

    public static final String INTEGRATION_NAME = "santander";

    private SantanderConstants() {
        throw new AssertionError();
    }

    public static final GenericTypeMapper<PaymentType, Pair<Type, Type>> PAYMENT_TYPE_MAPPER =
            GenericTypeMapper.<PaymentType, Pair<Type, Type>>genericBuilder()
                    .put(PaymentType.SEPA, new Pair<>(Type.IBAN, Type.IBAN))
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }

    public static class Urls {
        public static final URL TOKEN = new URL(Endpoints.BASE_URL + Endpoints.TOKEN);
        public static final URL CONSENT = new URL(Endpoints.BASE_URL + Endpoints.CONSENT);
        public static final URL ACCOUNTS = new URL(Endpoints.BASE_URL + Endpoints.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(Endpoints.BASE_URL + Endpoints.BASE_AIS);
        public static final URL SEPA_PAYMENT = new URL(Endpoints.BASE_URL + Endpoints.SEPA_PAYMENT);
        public static final URL FETCH_PAYMENT =
                new URL(Endpoints.BASE_URL + Endpoints.FETCH_PAYMENT);
    }

    public static class Endpoints {
        public static final String BASE_URL = "https://apigateway-sandbox.api.santander.de";
        public static final String TOKEN = "/scb-openapis/sx/oauthsos/password/token";
        public static final String CONSENT = "/scb-openapis/sx/v1/consents";
        public static final String ACCOUNTS = "/scb-openapis/sx/v1/accounts";
        public static final String BASE_AIS = "/scb-openapis/sx";
        public static final String PIS_PRODUCT = "/scb-openapis/sx/v1";
        public static final String SEPA_PAYMENT = PIS_PRODUCT + "/payments/sepa-credit-transfers";
        public static final String FETCH_PAYMENT =
                PIS_PRODUCT + "/payments/{paymentProduct}/{paymentId}/";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String CONSENT_ID = "consentId";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class IdTag {
        public static final String PAYMENT_ID = "paymentId";
        public static final String PAYMENT_PRODUCT = "paymentProduct";
        public static final String SEPA_PAYMENT = "sepa-credit-transfers";
    }

    public static class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String WITH_BALANCE = "withBalance";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String GRANT_TYPE = "grant_type";
    }

    public static class QueryValues {
        public static final String GRANT_TYPE = "client_credentials";
        public static final String SCOPE = "AIS:";
        public static final String RESPONSE_TYPE = "code";
        public static final String CODE_CHALLENGE_METHOD = "S256";
        public static final String BOTH = "both";
        public static final String TRUE = "true";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String X_IBM_CLIENT_ID = "X-IBM-Client-Id";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class HeaderValues {
        public static final String PSU_IP_ADDRESS = "192.168.0.1";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }

    public static class PaymentDetails {
        public static final String CREDITOR_NAME = "Creditor name";
        public static final String CREDITOR_AGENT = "Creditor agent";
        public static final String END_TO_END_IDENTIFICATION = "EndToEndIdentification";
        public static final String REMMITANCE_INFORMATION = "Invoice";
    }
}
