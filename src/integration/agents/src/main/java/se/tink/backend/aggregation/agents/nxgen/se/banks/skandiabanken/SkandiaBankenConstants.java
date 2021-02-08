package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken;

import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.transfer.enums.TransferType;

public class SkandiaBankenConstants {
    public static final String CURRENCY = "SEK";

    public static class Urls {
        public static final String BASE = "https://api.skandia.se";
        public static final String AUTH_BASE = "https://fsts.skandia.se";
        public static final String LOGIN_BASE = "https://login.skandia.se";

        public static final URL INIT_TOKEN = new URL(BASE + Endpoints.INIT_TOKEN);
        public static final URL CREATE_SESSION = new URL(BASE + Endpoints.CREATE_SESSION);
        public static final URL OAUTH_AUTHORIZE = new URL(AUTH_BASE + Endpoints.OAUTH_AUTHORIZE);
        public static final URL OAUTH_AUTOSTART_AUTHORIZE =
                new URL(LOGIN_BASE + Endpoints.OAUTH_AUTOSTART_AUTHORIZE);
        public static final URL OAUTH_CHOOSER_AUTHORIZE =
                new URL(LOGIN_BASE + Endpoints.OAUTH_CHOOSER_AUTHORIZE);
        public static final URL BANKID_COLLECT = new URL(LOGIN_BASE + Endpoints.BANKID_COLLECT);
        public static final URL FETCH_AUTH_TOKEN = new URL(BASE + Endpoints.FETCH_AUTH_TOKEN);
        public static final URL FETCH_ACCOUNTS = new URL(BASE + Endpoints.FETCH_ACCOUNTS);
        public static final URL FETCH_ACCOUNT_TRANSACTIONS =
                new URL(BASE + Endpoints.FETCH_ACCOUNT_TRANSACTIONS);
        public static final URL FETCH_PENDING_ACCOUNT_TRANSACTIONS =
                new URL(BASE + Endpoints.FETCH_PENDING_ACCOUNT_TRANSACTIONS);
        public static final URL FETCH_CREDIT_CARDS = new URL(BASE + Endpoints.FETCH_CREDIT_CARDS);
        public static final URL FETCH_INVESTMENT_ACCOUNTS =
                new URL(BASE + Endpoints.FETCH_INVESTMENT_ACCOUNTS);
        public static final URL FETCH_INVESTMENT_ACCOUNT_DETAILS =
                new URL(BASE + Endpoints.FETCH_INVESTMENT_ACCOUNT_DETAILS);
        public static final URL FETCH_INVESTMENT_HOLDINGS =
                new URL(BASE + Endpoints.FETCH_INVESTMENT_HOLDINGS);
        public static final URL FETCH_PENSIONS_HOLDINGS =
                new URL(BASE + Endpoints.FETCH_PENSIONS_HOLDINGS);
        public static final URL FETCH_IDENTITY = new URL(BASE + Endpoints.FETCH_IDENTITY);
        public static final URL FETCH_APPROVED_PAYMENTS =
                new URL(BASE + Endpoints.FETCH_APPROVED_PAYMENTS);
        public static final URL LOGOUT = new URL(BASE + Endpoints.LOGOUT);
        public static final URL LOGIN_MESSAGE = new URL(LOGIN_BASE).concat("/message/");
    }

    public static class Endpoints {
        public static final String INIT_TOKEN = "/mobile/v1/oauth2/session/token";
        public static final String CREATE_SESSION = "/mobile/v1/session";
        public static final String OAUTH_AUTHORIZE = "/as/authorization.oauth2";
        public static final String OAUTH_AUTOSTART_AUTHORIZE =
                "/mobiltbankid/autostartauthenticate";
        public static final String OAUTH_CHOOSER_AUTHORIZE = "/mobiltbankid/fromchooserautostart/";
        public static final String BANKID_COLLECT = "/mobiltbankid/collecting";
        public static final String FETCH_AUTH_TOKEN = "/mobile/v1/oauth2/token";
        public static final String FETCH_ACCOUNTS = "/Customers/V2/Commitments/BankAccounts";
        public static final String FETCH_ACCOUNT_TRANSACTIONS =
                "/Accounts/V2/BankAccounts/Transactions/{accountId}/{page}/"
                        + Fetcher.TRANSACTIONS_PER_BATCH;
        public static final String FETCH_PENDING_ACCOUNT_TRANSACTIONS =
                "/Accounts/V2/BankAccounts/Reservations/{accountId}";
        public static final String FETCH_CREDIT_CARDS = "/Customers/V3/Commitments/Cards";
        public static final String FETCH_INVESTMENT_ACCOUNTS =
                "/Customers/V2/Commitments/SecuritiesAccounts,Insurances,Pensions";
        public static final String FETCH_INVESTMENT_ACCOUNT_DETAILS =
                "/Securities/V2/Accounts/{accountId}";
        public static final String FETCH_INVESTMENT_HOLDINGS =
                "/Securities/V2/Holdings/{accountId}";
        public static final String FETCH_PENSIONS_HOLDINGS = "/Pensions/V2/Holdings/{partId}";
        public static final String FETCH_IDENTITY = "/Customers/V2/Customer";
        public static final String FETCH_APPROVED_PAYMENTS = "/Banking/V2/Payments/Approved";
        public static final String LOGOUT = "/mobile/v1/oauth2/token/revoke";
    }

    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAGE = "page";
        public static final String PART_ID = "partId";
    }

    public static class QueryParam {
        public static final String CODE_CHALLENGE_METHOD = "code_challenge_method";
        public static final String CODE_CHALLENGE_METHOD_S256 = "S256";
        public static final String RESPONSE_TYPE = "response_type";
        public static final String RESPONSE_TYPE_CODE = "code";
        public static final String CLIENT_ID = "client_id";
        public static final String CLIENT_ID_VALUE = "e_mobile_identified_short";
        public static final String CODE_CHALLENGE = "code_challenge";
        public static final String REDRIECT_URI = "redirect_uri";
        public static final String REDIRECT_URI_VALUE = "se.skandia.app:/oauth2/code";
        public static final String PFIDP_ADAPTER_ID = "pfidpadapterid";
        public static final String PFIDP_ADAPTER_ID_MOBILE = "MobileBankIdAppLogin";
        public static final String SCOPE = "scope";
        public static final String SCOPES_READ =
                "account_full account_quickbalance account_read app_offline bank_balance bank_full bank_microsavings bank_quicktransfer bank_roundup card_full card_read customer_full customer_read mobile_notify openid pension_full pension_read securities_full securities_read service_downgrade system_full system_read system_signcollect";
        public static final String STATE = "state";
        public static final String ENCRYPED_NATIONAL_IDENTIFICATION_NUMBER =
                "encrypedNationalIdentificationNumber";
    }

    public static class HeaderKeys {
        public static final String ADRUM = "ADRUM";
        public static final String ADRUM_1 = "ADRUM_1";
        public static final String SK_API_KEY = "Sk-Api-Key";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class HeaderValues {
        public static final String ADRUM = "isAjax:true";
        public static final String ADRUM_1 = "isMobile:true";
        public static final String SK_API_KEY =
                "HxWsuld1w9/Wjr/JnOau3gCpzQSUGpIXQ8dRFt5IB0T8E8HDBz3nzlxRT+8ssg9b";
    }

    public static class FormKeys {
        public static final String CLIENT_SECRET = "client_secret";
        public static final String SCOPE = "scope";
        public static final String CLIENT_ID = "client_id";
        public static final String GRANT_TYPE = "grant_type";
        public static final String REQUEST_TOKEN = "__RequestVerificationToken";
        public static final String REQUEST_WITH = "X-Requested-With";
        public static final String REDIRECT_URI = "redirect_uri";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String CODE = "code";
        public static final String REFRESH_TOKEN = "refresh_token";
    }

    public static class FormValues {
        public static final String REDIRECT_URI = "se.skandia.app:/oauth2/code";
        public static final String CLIENT_SECRET =
                "ow0yt2CUsmrxaX4yVyZsbVJpMKVCWEGSTjIv2enLobIOYJSRmVVMBFUEu3SACLHR";
        public static final String CLIENT_SECRET_FOR_BEARER =
                "j40lamrjJTFMI6k8MP5M6NiygdpUYroucw8bWpmUPs7xhVXsEMWAxOrhCCfSZLxI";
        public static final String SCOPE = "mobile_session mobile_readconfiguration";
        public static final String CLIENT_ID_MEDIUM = "e_mobile_anonymous_medium";
        public static final String CLIENT_ID_SHORT = "e_mobile_identified_short";
        public static final String GRANT_TYPE = "client_credentials";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String REQUEST_WITH = "XMLHttpRequest";
        public static final String GRANT_TYPE_FOR_BEARER = "authorization_code";
    }

    public static class StorageKeys {
        public static final String INIT_ACCESS_TOKEN = "init_access_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REQUEST_VER_TOKEN = "requestVerificationToken";
    }

    public static class Fetcher {
        public static final int START_PAGE = 1;
        public static final int TRANSACTIONS_PER_BATCH = 200;
    }

    public static class Authentication {
        public static final String CODE_VERIFIER_CHARSET =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~";
        public static final String REGISTRATION_TOKEN = "Qx84uM4y6UkKKVDwSbLJVfNz";
        public static final String INSTANCE_ID = "SNQ9XcUyKhNVeuwg6PddhY7w";
    }

    public static final class LogTags {
        public static final LogTag UPCOMING_TRANSFER = LogTag.from("se_skandia_upcoming_transfer");
    }

    public static final class ErrorMessages {
        public static final String STATUS_MESSAGE_INTERNAL_SERVER_ERROR = "InternalServerError";
        public static final String ERROR_CODE_MISSING_CREDIT_CARDS = "CUPGER0201";
        public static final String ERROR_MESSAGE_COMMUNICATION_FAILED = "Communication failed.";
        public static final String INVESTMENT_NUMBER_NOT_FOUND =
                "Investment account number was not found";
        public static final String NOT_CUSTOMER =
                "för att använda tjänsten behöver du bli kund hos oss.";
    }

    public static class PaymentStatus {
        public static final String APPROVED = "Approved";
    }

    public static class TimeoutRetryConfig {
        public static final int NUM_TIMEOUT_RETRIES = 3;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 1000;
    }

    public static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "AIE", "Euro")
                    .put(
                            TransactionalAccountType.SAVINGS,
                            "Savings",
                            "Payment",
                            "Solvency",
                            "FixedInterest")
                    .build();

    public static final TypeMapper<Instrument.Type> INSTRUMENT_TYPE_MAP =
            TypeMapper.<Instrument.Type>builder()
                    .put(Instrument.Type.FUND, "FUND")
                    .put(Instrument.Type.STOCK, "STO")
                    .build();

    public static final TypeMapper<Portfolio.Type> PORTFOLIO_TYPE_MAP =
            TypeMapper.<Portfolio.Type>builder()
                    .put(Portfolio.Type.DEPOT, "AIE DEPÅ/INTERNET")
                    .put(Portfolio.Type.ISK, "ISK")
                    .build();

    public static final TypeMapper<AccountIdentifier.Type> PAYMENT_RECIPIENT_TYPE_MAP =
            TypeMapper.<AccountIdentifier.Type>builder()
                    .put(Type.SE_BG, "BankGiro", "BankGiroOCR", "BankGiroInvoice")
                    .put(Type.SE_PG, "PlusGiro", "PlusGiroOCR", "PlusGiroInvoice")
                    .put(
                            Type.SE,
                            "AutoGiroAvi",
                            "AutoGiroRecurrent",
                            "CreditCardInvoice",
                            "SingleTransfer")
                    .build();

    public static final TypeMapper<TransferType> PAYMENT_TRANSFER_TYPE_MAP =
            TypeMapper.<TransferType>builder()
                    .put(TransferType.EINVOICE, "BankGiroOCR", "BankGiroInvoice", "PlusGiroInvoice")
                    .put(TransferType.BANK_TRANSFER, "SingleTransfer")
                    .put(
                            TransferType.PAYMENT,
                            "AutoGiroRecurrent",
                            "CreditCardInvoice",
                            "PlusGiroOCR",
                            "BankGiro")
                    .build();
}
