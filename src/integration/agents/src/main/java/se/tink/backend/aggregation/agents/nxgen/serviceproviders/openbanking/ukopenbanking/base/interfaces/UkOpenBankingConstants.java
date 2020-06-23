package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.UkOpenBankingAisAuthenticatorConstants;

public abstract class UkOpenBankingConstants {

    protected UkOpenBankingConstants() {
        throw new AssertionError();
    }

    public static class HttpHeaders {
        public static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
        public static final String X_JWS_SIGNATURE = "x-jws-signature";
    }

    public static class PartyEndpoints {
        public static final String IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES =
                "/accounts/%s/parties";
        public static final String IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY = "/accounts/%s/party";
        public static final String IDENTITY_DATA_ENDPOINT_PARTY = "/party";
        public static ImmutableMap<String, String> partyEndpointsPermissionMap =
                ImmutableMap.<String, String>builder()
                        .put(
                                IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY,
                                UkOpenBankingAisAuthenticatorConstants
                                        .ACCOUNT_PERMISSION_READ_PARTY)
                        .put(
                                IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES,
                                UkOpenBankingAisAuthenticatorConstants
                                        .ACCOUNT_PERMISSION_READ_PARTY)
                        .put(
                                IDENTITY_DATA_ENDPOINT_PARTY,
                                UkOpenBankingAisAuthenticatorConstants
                                        .ACCOUNT_PERMISSION_READ_PARTY_PSU)
                        .build();
    }

    public static class ApiServices {
        public static final String ACCOUNT_BULK_REQUEST = "/accounts";
        public static final String ACCOUNT_BALANCE_REQUEST = "/accounts/%s/balances";
        public static final String ACCOUNT_TRANSACTIONS_REQUEST = "/accounts/%s/transactions";
        public static final String ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST =
                "/accounts/%s/scheduled-payments";
        public static final String ACCOUNT_BENEFICIARIES_REQUEST = "/accounts/%s/beneficiaries";
        public static final String PAYMENTS = "/payments";
        public static final String PAYMENT_SUBMISSIONS = "/payment-submissions";

        public static class Domestic {
            public static final String PAYMENT_CONSENT = "/domestic-payment-consents";
            public static final String PAYMENT_CONSENT_STATUS =
                    "/domestic-payment-consents/{consentId}";
            public static final String PAYMENT_FUNDS_CONFIRMATION =
                    "/domestic-payment-consents/{consentId}/funds-confirmation";
            public static final String PAYMENT = "/domestic-payments";
            public static final String PAYMENT_STATUS = "/domestic-payments/{paymentId}";
        }

        public static class International {
            public static final String PAYMENT_CONSENT = "/international-payment-consents";
            public static final String PAYMENT_CONSENT_STATUS =
                    "/international-payment-consents/{consentId}";
            public static final String PAYMENT_FUNDS_CONFIRMATION =
                    "/international-payment-consents/{consentId}/funds-confirmation";
            public static final String PAYMENT = "/international-payments";
            public static final String PAYMENT_STATUS = "/international-payments/{paymentId}";
        }

        public static class UrlParameterKeys {
            public static final String CONSENT_ID = "consentId";
            public static final String PAYMENT_ID = "paymentId";
        }
    }

    public static class JWTSignatureHeaders {
        public static class HEADERS {
            public static final String IAT = "http://openbanking.org.uk/iat";
            public static final String ISS = "http://openbanking.org.uk/iss";
            public static final String TAN = "http://openbanking.org.uk/tan";
            public static final String B64 = "b64";
            public static final String CRIT = "crit";
        }

        public static class PAYLOAD {
            public static final String DATA = "Data";
            public static final String RISK = "Risk";
        }
    }
}
