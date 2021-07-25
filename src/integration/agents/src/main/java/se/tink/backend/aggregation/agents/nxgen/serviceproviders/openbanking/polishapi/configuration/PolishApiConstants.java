package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions.TransactionTypeRequest.DONE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions.TransactionTypeRequest.PENDING;

import com.google.common.collect.ImmutableList;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;

@UtilityClass
public class PolishApiConstants {

    @UtilityClass
    public static class Authorization {
        public static final String BEARER = "Bearer";

        @UtilityClass
        public static class Common {

            public static final int CONSENT_LENGTH = 90;

            @UtilityClass
            public static class GrantTypes {
                public static final String AUTHORIZATION_CODE = "authorization_code";
                public static final String REFRESH_TOKEN = "refresh_token";
                public static final String EXCHANGE_TOKEN = "exchange_token";
            }

            @UtilityClass
            public static class Scopes {
                public static final String AIS = "ais";
                public static final String AIS_ACCOUNTS = "ais-accounts";
            }

            public static final String SCOPE_USAGE_LIMIT_MULTIPLE = "multiple";
            public static final String SCOPE_USAGE_LIMIT_SINGLE = "single";
            public static final String CODE = "code";
        }

        @UtilityClass
        public static class GetClient {
            public static final String AIS = "AIS";
            public static final String AIS_ACCOUNTS = "AIS_ACCOUNTS";
            public static final String THROTTLING_POLICY = "PSD2_REGULATORY";
        }

        @UtilityClass
        public static class PostClient {
            public static final String THROTTLING_POLICY = "psd2Regulatory";
            public static final String AUTHORIZATION_MODE = "extended";
        }
    }

    @UtilityClass
    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
    }

    @UtilityClass
    public static class Headers {

        @UtilityClass
        public static class HeaderKeys {
            public static final String ACCEPT = "Accept";
            public static final String ACCEPT_CHARSET = "Accept-Charset";
            public static final String ACCEPT_ENCODING = "Accept-Encoding";
            public static final String ACCEPT_LANGUAGE = "Accept-Language";
            public static final String AUTHORIZATION = "Authorization";
            public static final String CLIENT_ID = "Client-ID";
            public static final String CONTENT_TYPE = "Content-Type";
            public static final String COMPANY_CONTEXT = "Company-Context";
            public static final String DATE = "Date";
            public static final String JWS_SIGNATURE = "JWS-SIGNATURE";
            public static final String X_JWS_SIGNATURE = "X-JWS-SIGNATURE";
            public static final String X_REQUEST_ID = "X-REQUEST-ID";
            public static final String X_IBM_CLIENT_ID = "X-IBM-Client-Id";
            public static final String X_IBM_CLIENT_SECRET = "X-IBM-Client-Secret";

            @UtilityClass
            public static class GetClient {
                public static final String TPP_REQUEST_ID = "TPP-Request-ID";
                public static final String PSU_USER_AGENT = "PSU-User-Agent";
                public static final String PSU_IP_ADDRESS = "PSU-IP-Address";
                public static final String PSU_IP_PORT = "PSU-IP-Port";
                public static final String PSU_SESSION = "PSU-Session";
                public static final String TPP_ID = "TPP-ID";
            }
        }

        @UtilityClass
        public static class HeaderValues {

            public static final String ACCEPT_CHARSET_VAL = "utf-8";
            public static final String ACCEPT_ENCODING_VAL = "deflate";

            @UtilityClass
            public static class GetClient {
                public static final String PSU_USER_AGENT_VAL = "Tink";
                public static final String PSU_IP_PORT_VAL = "23456";
                public static final String ACCEPT_VAL = "application/json; charset=utf-8";
                public static final String CONTENT_TYPE_VAL = "application/json";
            }
        }
    }

    @UtilityClass
    public static class JwsHeaders {
        private static final String AUTHORIZATION = "authorization";
        private static final String CLIENT_ID = "client-id";
        private static final String COMPANY_CONTEXT = "company-context";
        private static final String PSU_CONTEXT_ID_TYPE = "psu-context-id-type";
        private static final String PSU_CONTEXT_ID = "psu-context-id";
        private static final String PSU_ID_TYPE = "psu-id-type";
        private static final String PSU_ID = "psu-id";
        private static final String PSU_IP_ADDRESS = "psu-ip-address";
        private static final String PSU_IP_POST = "psu-ip-port";
        private static final String PSU_SESSION = "psu-session";
        private static final String PSU_USER_AGENT = "psu-user-agent";
        private static final String SEND_DATE = "send-date";
        private static final String TPP_BUNDLE_ID = "tpp-bundle-id";
        private static final String TPP_ID = "tpp-id";
        private static final String TPP_PAYMENT_ID = "tpp-payment-id";
        private static final String TPP_REQUEST_ID = "tpp-request-id";

        private static final List<String> JWS_HEADERS =
                Arrays.asList(
                        AUTHORIZATION,
                        CLIENT_ID,
                        COMPANY_CONTEXT,
                        PSU_CONTEXT_ID_TYPE,
                        PSU_CONTEXT_ID_TYPE,
                        PSU_CONTEXT_ID,
                        PSU_ID_TYPE,
                        PSU_ID,
                        PSU_IP_ADDRESS,
                        PSU_IP_POST,
                        PSU_SESSION,
                        PSU_USER_AGENT,
                        SEND_DATE,
                        TPP_BUNDLE_ID,
                        TPP_ID,
                        TPP_PAYMENT_ID,
                        TPP_REQUEST_ID);

        public static List<String> getJwsHeaders() {
            return Collections.unmodifiableList(JWS_HEADERS);
        }
    }

    @UtilityClass
    public static class Localization {
        private static final String PL_LANGUAGE = "pl";
        private static final String EN_LANGUAGE = "en";

        public static String getLanguageCode(String userLocale) {
            String userLanguageCode =
                    Optional.ofNullable(StringUtils.left(userLocale, 2)).orElse(PL_LANGUAGE);

            return EN_LANGUAGE.equalsIgnoreCase(userLanguageCode) ? EN_LANGUAGE : PL_LANGUAGE;
        }

        public static final DateTimeFormatter DATE_TIME_FORMATTER_TRANSACTIONS =
                DateTimeFormatter.ISO_LOCAL_DATE;

        public static final DateTimeFormatter DATE_TIME_FORMATTER_REQUEST_HEADERS =
                DateTimeFormatter.ISO_INSTANT;

        public static final DateTimeFormatter DATE_TIME_FORMATTER_HEADERS =
                DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss 'GMT'");
    }

    @UtilityClass
    public static class StorageKeys {
        public static final String TOKEN = OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN;
        public static final String ACCOUNTS = "Accounts";
        public static final String ACCOUNT_IDENTIFIERS = "accountNumbers";
        public static final String CONSENT_ID = "ConsentId";
    }

    @UtilityClass
    public static class Transactions {

        public enum TransactionTypeRequest {
            DONE,
            PENDING,

            // currently our data model does not support below transaction types - no need to fetch
            // them.
            SCHEDULED,
            REJECTED,
            HOLD,
            CANCELLED;

            public String getPostTransactionsEndpoint() {
                return "getTransactions" + StringUtils.capitalize(name().toLowerCase());
            }
        }

        public static final List<TransactionTypeRequest> SUPPORTED_TRANSACTION_TYPES =
                ImmutableList.of(DONE, PENDING);

        public static final int PAGE_SIZE = 100;

        @UtilityClass
        public static class GetClient {

            public static class QueryParams {
                public static final String TRANSACTION_DATE_FROM = "transactionDateFrom";
                public static final String TRANSACTION_DATE_TO = "transactionDateTo";
                public static final String PAGE_SIZE = "pageSize";
                public static final String TRANSACTION_ID_FROM = "transactionIdFrom";
                public static final String TRANSACTION_STATUS = "transactionStatus";
            }
        }
    }

    @UtilityClass
    public static class Accounts {
        public static final String CORPORATION = "CORPORATION";

        public static boolean isIndividualAccount(String accountHolderType) {
            return !CORPORATION.equalsIgnoreCase(accountHolderType);
        }

        public static final int PAGE_SIZE = 100;

        public enum HolderRole {
            OWNER,
            BORROWER,
            GUARANTOR,
            PROXY_OWNER,
            BENEFICIARY,
            TRUSTEE
        }
    }

    @UtilityClass
    public static class Logs {
        public static final LogTag LOG_TAG = LogTag.from("[Polish API]");
    }
}
