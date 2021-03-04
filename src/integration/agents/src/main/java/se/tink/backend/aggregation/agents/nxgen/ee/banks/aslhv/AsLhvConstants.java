package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AsLhvConstants {

    public static final ThreadSafeDateFormat DATE_FORMAT = ThreadSafeDateFormat.FORMATTER_DAILY;

    public static final TransactionalAccountTypeMapper TRANSACTIONAL_ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "11001")
                    .put(TransactionalAccountType.SAVINGS, "11002")
                    .build();

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder().put(AccountTypes.CREDIT_CARD, "11009").build();

    public static class Storage {
        public static final String CURRENT_USER = "currentUser";
        public static final String CURRENCIES = "currencies";
        public static final String BASE_CURRENCY_ID = "baseCurrencyId";
        public static final String USER_DATA = "userData";
    }

    public static class URLS {
        public static final String SERVICE_ENDPOINT = "/services/external/mobile/1.5";
        public static final String JSON_ENDPOINT = "/json/";
        public static final String AUTH_ENDPOINT = "/auth";
        public static final String PORTFOLIO_ENDPOINT = "/portfolio";
        public static final String GET_USER_DATA_ENDPOINT =
                PORTFOLIO_ENDPOINT + "/get_user_data" + JSON_ENDPOINT;
        public static final String GET_CURRENCIES_ENDPOINT =
                PORTFOLIO_ENDPOINT + "/get_currencies" + JSON_ENDPOINT;
        public static final String GET_ACCOUNT_TRANSACTIONS_ENDPOINT =
                PORTFOLIO_ENDPOINT + "/get_account_transaction_history" + JSON_ENDPOINT;
        public static final String AUTH_IS_AUTHENTICATED_ENDPOINT =
                AUTH_ENDPOINT + "/is_authenticated" + JSON_ENDPOINT;
        public static final String AUTH_PASSWORD_ENDPOINT =
                AUTH_ENDPOINT + "/login_password" + JSON_ENDPOINT;
        public static final String AUTH_LOGOUT_ENDPOINT = AUTH_ENDPOINT + "/logout" + JSON_ENDPOINT;
        public static final String BASE_URL = "www.lhv.ee";
    }

    public static class Header {
        public static final String ACCEPT_ALL = "*/*";
        public static final String ACCEPT_JSON = "application/json";
        public static final String ACCEPT_LANGUAGE = "us;q=1";
        public static final String CONTENT_TYPE_FORM_URLENCODED =
                "application/x-www-form-urlencoded";
        public static final String LHV_APPLICATION_LANGUAGE_HEADER = "LHV-Application-Language";
        public static final String LHV_APPLICATION_LANUGAGE_US = "us";
    }

    public static class Form {
        public static final String USERNAME_PARAMETER = "nickname";
        public static final String PASSWORD_PARAMETER = "password";
        public static final String FROM_DATE = "date_start";
        public static final String TO_DATE = "date_end";
        public static final String PORTFOLIO_ID = "portfolio_id";
    }

    public static class Messages {
        public static final String INCORRECT_CREDENTIALS = "Incorrect username or password.";
        public static final String INVALID_PARAMETERS =
                "Invalid parameters - please check the marked fields.";
    }
}
