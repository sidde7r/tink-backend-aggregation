package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class KbcConstants {

    public static final List<String> CHECKING_ACCOUNT_KEYS =
            ImmutableList.of(
                    "CACC",
                    "CASH",
                    "CHAR",
                    "CISH",
                    "COMM",
                    "SLRY",
                    "TRAN",
                    "TRAS",
                    "CurrentAccount",
                    "Current");

    public static final List<String> SAVING_ACCOUNT_KEYS = ImmutableList.of("LLSV", "ONDP", "SVGS");

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            CHECKING_ACCOUNT_KEYS.toArray(new String[0]))
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            SAVING_ACCOUNT_KEYS.toArray(new String[0]))
                    .build();

    private KbcConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_AUTH_URL = "https://idp.kbc.com";
        public static final String BASE_URL = "https://openapi.kbc-group.com";
        public static final String AIS_PRODUCT = "/psd2";
        private static final String BASE_AUTH = "/ASK/oauth";
        public static final String AUTH = BASE_AUTH + "/authorize/1";
        public static final String TOKEN = BASE_AUTH + "/token/1";
        private static final String BASE_AIS = AIS_PRODUCT + "/v2";
        public static final String CONSENT = BASE_AIS + "/consents";
        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String BASE_PIS = "/psd2/v2";
        public static final String PAYMENTS = BASE_PIS + "/payments/{paymentProduct}";
        public static final String PAYMENT_STATUS = PAYMENTS + "/{paymentId}/status";
        public static final String TRANSACTIONS = ACCOUNTS + "/{account-id}/transactions";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "account-id";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
        public static final String STATE = "STATE";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String BOOKED = "booked";
    }

    public static class CredentialKeys {
        public static final String IBAN = "iban";
    }

    public static class OAuth {
        public static final String BEARER = "Bearer";
    }

    public static class ErrorMessages {
        public static final String WRONG_PAYMENT_METHOD =
                "Wrong method used for payment initiation.";
    }

    public static class RegexValues {
        public static final String IBAN = "^BE[0-9]{14}$";
    }
}
