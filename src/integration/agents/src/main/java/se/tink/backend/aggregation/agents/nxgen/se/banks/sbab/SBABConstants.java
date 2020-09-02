package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class SBABConstants {

    public SBABConstants() {
        throw new AssertionError();
    }

    public static final String CURRENCY = "SEK";

    public static class Urls {
        public static final String HOST = "https://api.sbab.se";
        public static final String BASE_URL = HOST + "/mobile-bff/api";
    }

    public static final ImmutableMap<String, Type> LOAN_TYPES =
            ImmutableMap.<String, Type>builder().put("MORTGAGE_LOAN", Type.MORTGAGE).build();

    public static final ImmutableMap<String, TransactionTypes> TRANSACTION_TYPES =
            ImmutableMap.<String, TransactionTypes>builder()
                    .put("WITHDRAWAL", TransactionTypes.WITHDRAWAL)
                    .put("DEPOSIT", TransactionTypes.DEFAULT)
                    .build();

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
    }

    public static final ImmutableMap<String, Object> HEADERS =
            ImmutableMap.<String, Object>builder()
                    .put("sbab-fingerprint", "iOS iPhone 13.3.1")
                    .put("client-version", "production 2.3 1 2020-05-01T09:16:17Z se.sbab.bankapp")
                    .build();

    public static class FormKeys {
        public static final String CLIENT_ID = "clientid";
        public static final String PENDING_CODE = "pending_code";
        public static final String GRANT_TYPE = "grant_type";
    }

    public static class FormValues {
        public static final String CLIENT_ID = "bapp";
        public static final String GRANT_TYPE = "app_pending_authorization_code";
    }

    public static class StorageKeys {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String BEARER_TOKEN = "bearer_token";
        public static final String ACCOUNT_RESPONSE = "accountResponse";
        public static final String ACCOUNTS_ENDPOINT = "accountsEndpoint";
    }

    public static class HrefKeys {
        public static final String AUTHORIZE = "authorize";
        public static final String TOKEN = "token";
        public static final String OVERVIEW = "overview";
    }

    public static class ErrorMessages {
        public static final String NO_CLIENT =
                "Du har inte BankID som går att använda för den här inloggningen på den här enheten.";
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.SAVINGS, "SAVINGS_ACCOUNT")
                    .build();
}
