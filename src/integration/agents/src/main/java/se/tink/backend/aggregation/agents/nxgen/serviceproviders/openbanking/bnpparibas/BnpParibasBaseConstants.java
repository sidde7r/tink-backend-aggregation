package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.enums.AccountFlag;

public final class BnpParibasBaseConstants {

    public static class Urls {
        public static final String ACCOUNTS_PATH = "/accounts";
        public static final String BALANCES_PATH = "/accounts/{accountResourceId}/balances";
        public static final String TRANSACTIONS_PATH = "/accounts/{accountResourceId}/transactions";
        public static final String FETCH_USER_IDENTITY_DATA = "/end-user-identity";
        public static final String CREATE_PAYMENT = "/payment-requests";
        public static final String GET_PAYMENT = "/payment-requests/{paymentId}";
    }

    public class QueryKeys {
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_SECRET = "client_secret";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String SCOPE = "scope";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String STATE = "state";
        public static final String GRANT_TYPE = "grant_type";
        public static final String CODE = "code";
        public static final String BEARER = "Bearer";
        public static final String REFRESH_TOKEN = "refresh_token";

        public static final String DATE_FROM = "dateFrom";
        public static final String DATE_TO = "dateTo";
    }

    public class QueryValues {
        public static final String CODE = "code";
        public static final String FULL_SCOPES = "aisp extended_transaction_history";
        public static final String PISP_SCOPE = "pisp";
        public static final String AUTHORIZATION_CODE = "authorization_code";
        public static final String CLIENT_CREDENTIALS = "client_credentials";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String TIMEZONE = "CET";
    }

    public class ErrorMessages {
        public static final String MISSING_CONFIGURATION = "Bnp Paribas configuration missing";
        public static final String MISSING_TOKEN = "Cannot find token";
    }

    public class HeaderKeys {
        public static final String AUTHORIZATION = "Authorization";
        public static final String SIGNATURE = "signature";
        public static final String X_REQUEST_ID = "x-request-id";
    }

    public class HeaderValues {
        public static final String BASIC = "Basic ";
    }

    public class StorageKeys {
        public static final String TOKEN = "OAUTH_TOKEN";
        public static final String PISP_TOKEN = "PISP_OAUTH_TOKEN";
        public static final String PAYMENT_AUTHORIZATION_URL = "PAYMENT_AUTHORIZATION_URL";
    }

    public class IdTags {
        public static final String ACCOUNT_RESOURCE_ID = "accountResourceId";
        public static final String BANK = "bank";
        public static final String PAYMENT_ID = "paymentId";
    }

    public class SignatureKeys {
        public static final String KEY_ID = "keyId";
        public static final String ALGORITHM = "algorithm";
        public static final String RSA_256 = "rsa-sha256";
        public static final String SIGNATURE = "signature";
        public static final String headers = "headers";
        public static final String X_REQUEST_ID = "x-request-id";
        public static final String AUTHORIZATION = "Authorization";
    }

    public class ResponseValues {
        public static final String PENDING_TRANSACTION = "pdng";
        public static final String BALANCE_TYPE_OTHER = "OTHR";
        public static final String BALANCE_TYPE_CLOSING = "CLBD";
        public static final String BALANCE_TYPE_EXPECTED = "XPCD";
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(
                            TransactionalAccountType.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "CACC",
                            "CARD",
                            "CASH",
                            "CHAR",
                            "CISH",
                            "COMM",
                            "SLRY",
                            "TRAN",
                            "TRAS",
                            "CurrentAccount",
                            "Current")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "LLSV",
                            "ONDP",
                            "SVGS")
                    .build();

    public static class RegisterUtils {
        public static final String REGISTER_URL =
                "https://api-psd2.bddf.bnpparibas/as/psd2/register";

        public static final String CONTENT_TYPE = "Content-Type";
        public static final String TOKEN_ENDPOINT_AUTH_METHOD = "tls_client_auth";
        public static final String SIGNATURE = "Signature";
        public static final String CLIENT_NAME = "TinkTest";
        public static final String PROVIDER_LEGAL_ID = "PSDSE-FINA-44059";
        public static final String CONTEXT = "psd2";
        public static final String SCOPES = "aisp pisp";
        public static final String CRYPT_ALG_FAMILY = "RSA";
        public static final List<String> CONTACTS =
                Collections.singletonList("openbanking@tink.se");
        public static final List<String> GRANT_TYPES =
                Arrays.asList(
                        "authorization_code", "refresh_token", "client_credentials", "password");
        public static final List<String> X5C =
                Collections.singletonList(
                        "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");
        public static final List<String> REDIRECT_URIS =
                Arrays.asList(
                        "https://127.0.0.1:7357/api/v1/thirdparty/callback",
                        "https://api.tink.com/api/v1/credentials/third-party/callback",
                        "https://api.tink.se/api/v1/credentials/third-party/callback",
                        "https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback");
    }
}
