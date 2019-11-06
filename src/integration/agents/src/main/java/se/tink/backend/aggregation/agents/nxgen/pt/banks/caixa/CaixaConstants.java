package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa;

import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CaixaConstants {

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "DepositAccount")
                    .put(TransactionalAccountType.SAVINGS, "TermSavingsAccount")
                    .build();

    public static class Urls {
        public static final String BASE = "https://app.cgd.pt/pceApi/rest/v1";

        public static final URL AUTH = new URL(BASE.concat(Endpoints.AUTH));
        public static final URL FETCH_ACCOUNTS = new URL(BASE.concat(Endpoints.FETCH_ACCOUNTS));
        public static final URL FETCH_ACCOUNT_DETAILS =
                new URL(BASE.concat(Endpoints.FETCH_ACCOUNT_DETAILS));
    }

    public static class Endpoints {
        public static final String AUTH = "/system/security/authentications/basic";
        public static final String FETCH_ACCOUNTS = "/business/accounts";
        public static final String FETCH_ACCOUNT_DETAILS = "/business/accounts/{accountKey}";
    }

    public static class Parameters {
        public static final String ACCOUNT_KEY = "accountKey";
    }

    public static class QueryParams {
        public static String USER = "u";
        public static String TARGET_OPERATION_TYPE = "targetOperationType";
        public static String INCLUDE_BALANCES = "includeBalancesInResponse";
        public static String INCLUDE_TRANSACTIONS = "includeTransactionsInResponse";
        public static String FROM = "fromBookDate";
        public static String TO = "toBookDate";
    }

    public static class QueryValues {
        public static String BALANCES_AND_TRANSACTIONS_OPERATION = "BALANCES_AND_TRANSACTIONS";
    }

    public static class HeaderKeys {
        public static final String X_CGD_APP_DEVICE = "X-CGD-APP-Device";
        public static final String X_CGD_APP_NAME = "X-CGD-APP-Name";
        public static final String X_CGD_APP_VERSION = "X-CGD-APP-Version";
    }

    public static class HeaderValues {
        public static final String ACCEPT = "*/*";
        public static final String DEVICE_NAME = "is2";
        public static final String APP_NAME = "APP_CAIXADIRECTA";
        public static final String APP_VERSION = "1.4.5";
    }
}
