package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class FidorConstants {

    public static final String INTEGRATION_NAME = "fidor";

    private FidorConstants() {
        throw new AssertionError();
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "Girokonto")
                    .build();

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
    }

    public static class Urls {
        public static final String BASE_URL = "https://xs2a.fidor.de";
        public static final String CONSENTS = BASE_URL + "/v1/consents";
        public static final String OAUTH_PASSWORD = BASE_URL + "/oauth/token";
        public static final String FETCH_ACCOUNTS = BASE_URL + "/v1/accounts";
        public static final String FETCH_BALANCES = BASE_URL + "/v1/accounts/%s/balances";
        public static final String FETCH_TRANSACTIONS = BASE_URL + "/v1/accounts/%s/transactions";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String CONSENT_ID = "CONSENT_ID";
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class Accounts {
        public static final String AVAILABLE_BALANCE = "interimAvailable";
    }

    public static class FieldKeys {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
        public static final String IBAN = "iban";
        public static final String BBAN = "bban";
    }

    public static class QueryKeys {
        public static final String GRANT_TYPE = "grant_type";
        public static final String PAGE = "page";
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";
    }

    public static class QueryValues {
        public static final String GRANT_TYPE = "password";
    }

    public static class HeaderKeys {
        public static final String X_REQUEST_ID = "X-Request-Id";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
        public static final String CONSENT_ID = "Consent-ID";
        public static final String TPP_REDIRECT_URI = "TPP-Redirect-URI";
        public static final String TPP_REDIRECT_PREFERRED = "TPP-Redirect-Preferred";
    }

    public static class HeaderValues {
        public static final String TPP_REDIRECT_PREFERRED = "true";
    }
}
