package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public final class SparebankConstants {

    public static final String INTEGRATION_NAME = "sparebank";

    private SparebankConstants() {
        throw new AssertionError();
    }

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            "Current",
                            "Current",
                            "Commission",
                            "TransactingAccount")
                    .put(TransactionalAccountType.SAVINGS, "Savings")
                    .setDefaultTranslationValue(TransactionalAccountType.OTHER)
                    .build();

    public static class Urls {
        public static final String CONSENTS = "/v1/consents";
        public static final String FETCH_ACCOUNTS = "/v1/accounts";
        public static final String FETCH_TRANSACTIONS = "/v1/accounts/%s/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String TPP_SESSION_ID = "TppSessionId";
        public static final String PSU_ID = "PsuId";
        public static final String TRANSACTIONS_URL = "TRANSACTIONS_URL";
    }

    public static class QueryKeys {
        public static final String WITH_BALANCE = "withBalance";
        public static final String LIMIT = "limit";
        public static final String OFFSET = "offset";
        public static final String BOOKING_STATUS = "bookingStatus";
    }

    public static class Accounts {
        public static final String BALANCE_CLOSING_BOOKED = "closingBooked";
    }

    public static class QueryValues {
        public static final String TRUE = "true";
        public static final String BOTH = "both";
        public static final String BOOKING_STATUS = "both";
    }

    public static class HeaderKeys {
        public static final String BOOKING_STATUS = "bookingStatus";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String X_REQUEST_ID = "X-Request-ID";
        public static final String DATE = "Date";
        public static final String TPP_SIGNATURE_CERTIFICATE = "TPP-Signature-Certificate";
        public static final String SIGNATURE = "Signature";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-Uri";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String AUTHORIZATION = "AUTHORIZATION";
        public static final String TPP_ID = "TPP-ID";
        public static final String TPP_SESSION_ID = "TPP-SESSION-ID";
        public static final String PSU_ID = "PSU-ID";
    }

    public static class HeaderValues {
        public static final String BOOKING_STATUS = "both";
    }

    public static class Signature {
        public static final String PSU_ID = "psu-id";
        public static final String DATE = "date";
        public static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
        public static final String HEADERS = "headers";
        public static final String SIGNING_ALGORITHM = "RSA";
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String ENCODE_CERTIFICATE_ERROR = "Cannot encode certificate.";
    }
}
