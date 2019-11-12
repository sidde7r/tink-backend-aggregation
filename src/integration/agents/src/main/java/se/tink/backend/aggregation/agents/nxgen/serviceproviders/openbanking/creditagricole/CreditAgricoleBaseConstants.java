package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class CreditAgricoleBaseConstants {

    public static final String INTEGRATION_NAME = "creditagricole";
    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CACC")
                    .ignoreKeys("CARD")
                    .build();

    private CreditAgricoleBaseConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
        public static final String INVALID_BALANCE_TYPE = "Balance type is not valid";
        public static final String UNABLE_LOAD_OAUTH_TOKEN = "Unable to load oauth_2_token";
        public static final String UNABLE_LOAD_BANK_URL = "Unable to load correct bank url";
    }

    public static class Urls {
        public static final String BASE_URL = "https://api.credit-agricole.fr";
    }

    public static class ApiServices {
        public static final String ACCOUNTS = "/dsp2/v1/accounts";
        public static final String CONSENTS = "/dsp2/v1/consents";
        public static final String TRANSACTIONS = "/dsp2/v1/accounts/{accountId}/transactions";
        public static final String FETCH_PAYMENT_REQUEST =
                "/dsp2/v1/payment-requests/{paymentRequestResourceId}";
        public static final String CREATE_PAYMENT_REQUEST = "/dsp2/v1/payment-requests/";
        public static final String TOKEN = "/authentication/v1/openid/token";
    }

    public static class StorageKeys {
        public static final String OAUTH_TOKEN = PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String STATE = "STATE";
        public static final String BANK_ENUM = "BANK_ENUM";
    }

    public static class HeaderKeys {
        public static final String CORRELATION_ID = "correlationid";
        public static final String CATS_CONSOMMATEUR = "cats_consommateur";
        public static final String CATS_CONSOMMATEURORIGINE = "cats_consommateurorigine";
        public static final String CATS_CANAL = "cats_canal";
        public static final String AUTHORIZATION = "Authorization";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String SIGNATURE = "signature";
        public static final String DIGEST = "Digest";
        public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
    }

    public static class HeaderValues {
        public static final String CATS_CONSOMMATEUR =
                "{\"consommateur\": {\"nom\": \"ING\", \"version\": \"1.0.0\"}}";
        public static final String CATS_CONSOMMATEURORIGINE =
                "{\"consommateur\": {\"nom\": \"ING\", \"version\": \"1.0.0\"}}";
        public static final String CATS_CANAL =
                "{\"canal\": {\"canalId\": \"internet\", \"canalDistribution\": \"internet\"}}";
        public static final String BEARER = "Bearer";
        public static final String BASIC = "Basic";
        public static final String DIGEST_PREFIX = "SHA-256=";
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

    public class QueryKeys {
        public static final String RESPONSE_TYPE = "response_type";
        public static final String CLIENT_ID = "client_id";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String SCOPE = "scope";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE = "code";
    }

    public class QueryValues {
        public static final String CODE = "code";
        public static final String SCOPE = "aisp extended_transaction_history";
        public static final String GRANT_TYPE = "authorization_code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public class SignatureKeys {
        public static final String KEY_ID = "keyId";
        public static final String HEADERS = "headers";
    }

    public class SignatureValues {
        public static final String RSA_SHA256 = "rsa-sha256";
        public static final String ALGORITHM = "algorithm";
    }

    public class Formats {
        public static final String SIGNATURE_STRING_FORMAT =
                "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"";
    }
}
