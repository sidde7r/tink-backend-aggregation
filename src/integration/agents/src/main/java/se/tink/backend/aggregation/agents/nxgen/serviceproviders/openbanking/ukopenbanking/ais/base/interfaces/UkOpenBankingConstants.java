package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface UkOpenBankingConstants {

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
