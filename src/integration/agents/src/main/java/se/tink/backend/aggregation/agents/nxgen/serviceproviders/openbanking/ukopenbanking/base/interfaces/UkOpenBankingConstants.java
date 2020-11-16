package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface UkOpenBankingConstants {

    class HttpHeaders {
        public static final String X_IDEMPOTENCY_KEY = "x-idempotency-key";
        public static final String X_JWS_SIGNATURE = "x-jws-signature";
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    }

    enum PartyEndpoint {
        IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES(
                "/accounts/%s/parties", PartyPermission.ACCOUNT_PERMISSION_READ_PARTY),
        IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY(
                "/accounts/%s/party", PartyPermission.ACCOUNT_PERMISSION_READ_PARTY),
        IDENTITY_DATA_ENDPOINT_PARTY("/party", PartyPermission.ACCOUNT_PERMISSION_READ_PARTY_PSU);

        private final String value;

        private final PartyPermission[] permissions;

        PartyEndpoint(String value, PartyPermission... partyPermissions) {
            this.value = value;
            this.permissions = partyPermissions;
        }

        public String getPath() {
            return this.value;
        }

        public Set<String> getPermissions() {
            return Stream.of(permissions)
                    .map(PartyPermission::getPermissionValue)
                    .collect(Collectors.toSet());
        }

        public enum PartyPermission {
            ACCOUNT_PERMISSION_READ_PARTY("ReadParty"),
            ACCOUNT_PERMISSION_READ_PARTY_PSU("ReadPartyPSU");

            private final String permissionValue;

            PartyPermission(String permissionValue) {
                this.permissionValue = permissionValue;
            }

            public String getPermissionValue() {
                return permissionValue;
            }
        }
    }

    class ApiServices {
        public static final String ACCOUNT_BULK_REQUEST = "/accounts";
        public static final String ACCOUNT_BALANCE_REQUEST = "/accounts/%s/balances";
        public static final String ACCOUNT_TRANSACTIONS_REQUEST = "/accounts/%s/transactions";
        public static final String ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST =
                "/accounts/%s/scheduled-payments";
        public static final String ACCOUNT_BENEFICIARIES_REQUEST = "/accounts/%s/beneficiaries";
        public static final String PAYMENTS = "/payments";

        public static class Domestic {
            public static final String PAYMENT_CONSENT = "/domestic-payment-consents";
            public static final String PAYMENT_CONSENT_STATUS =
                    "/domestic-payment-consents/{consentId}";
            public static final String PAYMENT_FUNDS_CONFIRMATION =
                    "/domestic-payment-consents/{consentId}/funds-confirmation";
            public static final String PAYMENT = "/domestic-payments";
            public static final String PAYMENT_STATUS = "/domestic-payments/{paymentId}";
        }

        public static class DomesticScheduled {
            public static final String PAYMENT_CONSENT = "/domestic-scheduled-payment-consents";
            public static final String PAYMENT_CONSENT_STATUS =
                    "/domestic-scheduled-payment-consents/{consentId}";
            public static final String PAYMENT = "/domestic-scheduled-payments";
            public static final String PAYMENT_STATUS = "/domestic-scheduled-payments/{paymentId}";
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

    class JWTSignatureHeaders {
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
