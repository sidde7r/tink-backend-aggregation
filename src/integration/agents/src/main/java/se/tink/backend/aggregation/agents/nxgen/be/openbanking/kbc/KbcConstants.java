package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public abstract class KbcConstants {

    public static final String INTEGRATION_NAME = "kbc";

    public static final String DEFAULT_IP = "0.0.0.0";

    public static String getPsuIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return KbcConstants.DEFAULT_IP;
        }
    }

    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            "CACC",
                            "CASH",
                            "CHAR",
                            "CISH",
                            "COMM",
                            "SLRY",
                            "TRAN",
                            "TRAS",
                            "CurrentAccount",
                            "Current")
                    .put(TransactionalAccountType.SAVINGS, "LLSV", "ONDP", "SVGS")
                    .build();

    private KbcConstants() {
        throw new AssertionError();
    }

    public static class Urls {
        public static final String BASE_AUTH_URL = "https://idp.kbc.com";
        public static final String AIS_PRODUCT = "/psd2";
        private static final String BASE_AUTH = "/ASK/oauth";
        public static final String AUTH = BASE_AUTH + "/authorize/1";
        public static final String TOKEN = BASE_AUTH + "/token/1";
        private static final String BASE_AIS = AIS_PRODUCT + "/v2";
        public static final String CONSENT = BASE_AIS + "/consents";
        public static final String ACCOUNTS = BASE_AIS + "/accounts";
        public static final String BASE_PIS = "/psd2/v2/";
        public static final String PAYMENTS = BASE_PIS + "/payments/{paymentProduct}";
        public static final String PAYMENT_STATUS = PAYMENTS + "/{paymentId}/status";
        public static final String AUTHORIZE_PAYMENT =
                BASE_PIS + "/authorization/payments/{paymentId}";
        public static final String TRANSACTIONS = ACCOUNTS + "/{account-id}/transactions";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "account-id";
    }

    public static class HeaderKeys {
        public static final String BEARER = "Bearer ";
    }

    public static class StorageKeys {
        public static final String ACCOUNT_ID = "ACCOUNT_ID";
    }

    public static class QueryKeys {
        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public static class QueryValues {
        public static final String BOOKED = "booked";
    }

    public static class CredentialKeys {
        public static final String IBAN = "IBAN";
    }
}
