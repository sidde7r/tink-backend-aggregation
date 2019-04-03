package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia;

import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.agents.rpc.AccountTypes;

public final class AktiaConstants {

    private AktiaConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.CHECKING, "Käyttötili").build();

    public static class Urls {
        public static final String BASE_URL = "https://api.aktia.fi";
        public static final String GET_ACCOUNTS =
                BASE_URL + "/api/openbanking/sandbox/psd2/ais/v1/accounts";
        public static final String GET_TRANSACTIONS =
                BASE_URL + "/api/openbanking/sandbox/psd2/ais/v1/accounts/{accountId}/transactions";
    }

    public static class StorageKeys {
        public static final String CLIENT_ID = "clientId";
        public static final String CLIENT_SECRET = "clientSecret";
        public static final String CONSENT_ID = "consentId";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String WITH_BALANCE = "withBalance";
    }

    public static class QueryValues {
        public static final String BOTH = "both";
        public static final String TRUE = "true";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String CONSENT_ID = "consent-id";
        public static final String X_IBM_CLIENT_ID = "x-ibm-client-id";
        public static final String X_IBM_CLIENT_SECRET = "x-ibm-client-secret";
    }

    public class Market {
        public static final String INTEGRATION_NAME = "aktia";
    }

    public class IdTags {
        public static final String ACCOUNT_ID = "accountId";
    }
}
