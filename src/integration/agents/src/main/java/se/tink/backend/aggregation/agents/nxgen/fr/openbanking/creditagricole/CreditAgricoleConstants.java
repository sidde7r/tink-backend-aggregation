package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.URL;

public final class CreditAgricoleConstants {

    public static final String INTEGRATION_NAME = "creditagricole";
    public static final TypeMapper<TransactionalAccountType> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<TransactionalAccountType>builder()
                    .put(TransactionalAccountType.CHECKING, "CACC")
                    .ignoreKeys("CARD")
                    .build();

    private CreditAgricoleConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String INVALID_BALANCE_TYPE = "Balance type is not valid";
    }

    public static class Urls {
        public static final String BASE_URL = "https://sandbox-api.credit-agricole.fr";

        public static final URL ACCOUNTS = new URL(BASE_URL + ApiServices.ACCOUNTS);
        public static final URL TRANSACTIONS = new URL(BASE_URL + ApiServices.TRANSACTIONS);
        public static final URL FETCH_PAYMENT_REQUEST =
                new URL(BASE_URL + ApiServices.FETCH_PAYMENT_REQUEST);
        public static final URL CREATE_PAYMENT_REQUEST =
                new URL(BASE_URL + ApiServices.CREATE_PAYMENT_REQUEST);
        public static final String SUCCESS_REPORT_PATH = "?code=123&state=";
    }

    public static class ApiServices {
        public static final String ACCOUNTS = "/dsp2/v1.5/accounts";
        public static final String TRANSACTIONS = "/dsp2/v1.5/accounts/{accountId}/transactions";
        public static final String FETCH_PAYMENT_REQUEST =
                "/dsp2/v1.5/payment-requests/{paymentRequestResourceId}";
        public static final String CREATE_PAYMENT_REQUEST = "/dsp2/v1.5/payment-requests/";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN;
        public static final String STATE = "STATE";
    }

    public static class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAYMENT_REQUEST_RESOURCE_ID = "paymentRequestResourceId";
    }

    public static class BalanceTypes {
        public static final String CLBD = "CLBD";
        public static final String XPCD = "XPCD";
    }

    public static class BookingStatus {
        public static final String PENDING = "PENDING";
    }

    public class DateFormat {
        public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    }

    public class FormValues {
        public static final String BENEFICIARY_NAME = "myMerchant";
        public static final String INSTRUCTION_ID = "MyInstrId";
    }
}
