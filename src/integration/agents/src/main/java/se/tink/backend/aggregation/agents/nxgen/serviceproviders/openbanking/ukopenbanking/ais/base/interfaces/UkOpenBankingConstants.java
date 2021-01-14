package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticatorConstants;

public interface UkOpenBankingConstants {

    enum PartyEndpoint {
        IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES(
                "/accounts/%s/parties", OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY),
        IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY(
                "/accounts/%s/party", OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY),
        IDENTITY_DATA_ENDPOINT_PARTY(
                "/party", OpenIdAuthenticatorConstants.ConsentPermission.READ_PARTY_PSU);

        private final String value;

        private final OpenIdAuthenticatorConstants.ConsentPermission permission;

        PartyEndpoint(String value, OpenIdAuthenticatorConstants.ConsentPermission permission) {
            this.value = value;
            this.permission = permission;
        }

        public String getPath() {
            return this.value;
        }

        public String getPermission() {
            return permission.getValue();
        }
    }

    class HttpHeaders {
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_FAPI_FINANCIAL_ID = "x-fapi-financial-id";
        public static final String X_FAPI_INTERACTION_ID = "x-fapi-interaction-id";
    }

    class ApiServices {
        public static final String ACCOUNT_BULK_REQUEST = "/accounts";
        public static final String ACCOUNT_BALANCE_REQUEST = "/accounts/%s/balances";
        public static final String ACCOUNT_TRANSACTIONS_REQUEST = "/accounts/%s/transactions";
        public static final String ACCOUNT_UPCOMING_TRANSACTIONS_REQUEST =
                "/accounts/%s/scheduled-payments";
        public static final String ACCOUNT_BENEFICIARIES_REQUEST = "/accounts/%s/beneficiaries";
    }
}
