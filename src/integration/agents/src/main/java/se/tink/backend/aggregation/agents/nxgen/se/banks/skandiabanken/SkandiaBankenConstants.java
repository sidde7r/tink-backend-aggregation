package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.transfer.enums.TransferType;

public class SkandiaBankenConstants {

    @AllArgsConstructor
    @Getter
    public enum Currency {
        SEK(1),
        EUR(2);
        private int currencyCode;

        public static Currency fromCode(int code) {
            for (Currency c : Currency.values()) {
                if (c.currencyCode == code) {
                    return c;
                }
            }
            return null;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Urls {
        private static final URL BASE = new URL("https://client-apis.skandia.se/mobile/external");
        private static final URL MOBILE_V1 = BASE.concat("/mobile/v1");
        private static final URL CUSTOMERS_V2 = BASE.concat("/api.customers.v2/api");
        private static final URL ACCOUNTS_V2 = BASE.concat("/api.accounts.v2/api");
        private static final URL SECURITIES_V2 = BASE.concat("/api.securities.v2/api");
        private static final URL PENSIONS_V2 = BASE.concat("/api.pensions.v2/api");
        private static final URL BANKING_V2 = BASE.concat("/api.banking.v2/api");

        private static final URL AUTH_BASE = new URL("https://fsts.skandia.se");
        private static final URL LOGIN_BASE = new URL("https://login.skandia.se");

        public static final URL INIT_TOKEN = MOBILE_V1.concat("/oauth2/session/token");
        public static final URL CREATE_SESSION = MOBILE_V1.concat("/session");
        public static final URL OAUTH_AUTHORIZE = AUTH_BASE.concat("/as/authorization.oauth2");
        public static final URL OAUTH_AUTOSTART_AUTHORIZE =
                LOGIN_BASE.concat("/mobiltbankid/autostartauthenticate");
        public static final URL OAUTH_CHOOSER_AUTHORIZE =
                LOGIN_BASE.concat("/mobiltbankid/fromchooserautostart/");
        public static final URL BANKID_COLLECT = LOGIN_BASE.concat("/mobiltbankid/collecting");

        public static final URL FETCH_AUTH_TOKEN = MOBILE_V1.concat("/oauth2/token");
        public static final URL FETCH_ACCOUNTS = CUSTOMERS_V2.concat("/Commitments/BankAccounts");
        public static final URL FETCH_ACCOUNT_TRANSACTIONS =
                ACCOUNTS_V2.concat("/BankAccounts/Transactions/{accountId}/{page}/{batchSize}");
        public static final URL FETCH_PENDING_ACCOUNT_TRANSACTIONS =
                ACCOUNTS_V2.concat("/BankAccounts/Reservations/{accountId}");
        public static final URL FETCH_CARDS = CUSTOMERS_V2.concat("/Commitments/Cards");
        public static final URL FETCH_INVESTMENT_ACCOUNTS =
                CUSTOMERS_V2.concat("/Commitments/SecuritiesAccounts,Insurances,Pensions");
        public static final URL FETCH_INVESTMENT_ACCOUNT_DETAILS =
                SECURITIES_V2.concat("/Accounts/{accountId}");
        public static final URL FETCH_INVESTMENT_HOLDINGS =
                SECURITIES_V2.concat("/Holdings/{accountId}");
        public static final URL FETCH_PENSIONS_HOLDINGS = PENSIONS_V2.concat("/Holdings/{partId}");
        public static final URL FETCH_IDENTITY = CUSTOMERS_V2.concat("/Customer");
        public static final URL FETCH_APPROVED_PAYMENTS = BANKING_V2.concat("/Payments/Approved");
        public static final URL LOGIN_MESSAGE = LOGIN_BASE.concat(Endpoints.MESSAGE);
        public static final URL LOGIN_OTP_CHOOSER = LOGIN_BASE.concat(Endpoints.OTP_CHOOSER);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Endpoints {
        public static final String MESSAGE = "/message/";
        public static final String OTP_CHOOSER = "/otpchooser/";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IdTags {
        public static final String ACCOUNT_ID = "accountId";
        public static final String PAGE = "page";
        public static final String PART_ID = "partId";
        public static final String BATCH_SIZE = "batchSize";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderKeys {
        public static final String ADRUM = "ADRUM";
        public static final String ADRUM_1 = "ADRUM_1";
        public static final String AUTHORIZATION = "Authorization";
        public static final String CLIENT_ID = "Client-Id";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class HeaderValues {
        public static final String ADRUM = "isAjax:true";
        public static final String ADRUM_1 = "isMobile:true";
        public static final String CLIENT_ID = "b21a8f57db9b1d3c50ed340380177668";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StorageKeys {
        public static final String INIT_ACCESS_TOKEN = "init_access_token";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String CODE_VERIFIER = "code_verifier";
        public static final String REQUEST_VER_TOKEN = "requestVerificationToken";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Fetcher {
        public static final int START_PAGE = 1;
        public static final int TRANSACTIONS_PER_BATCH = 200;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Authentication {
        public static final String CODE_VERIFIER_CHARSET =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-._~";
        public static final String REGISTRATION_TOKEN = "Qx84uM4y6UkKKVDwSbLJVfNz";
        public static final String INSTANCE_ID = "SNQ9XcUyKhNVeuwg6PddhY7w";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LogTags {
        public static final LogTag UPCOMING_TRANSFER = LogTag.from("se_skandia_upcoming_transfer");
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ErrorMessages {
        public static final String STATUS_MESSAGE_INTERNAL_SERVER_ERROR = "InternalServerError";
        public static final String ERROR_CODE_MISSING_CREDIT_CARDS = "CUPGER0201";
        public static final String ERROR_MESSAGE_COMMUNICATION_FAILED = "Communication failed.";
        public static final String INVESTMENT_NUMBER_NOT_FOUND =
                "Investment account number was not found";
        public static final String NOT_CUSTOMER =
                "för att använda vår app behöver du ha ett bankkonto";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PaymentStatus {
        public static final String APPROVED = "Approved";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TimeoutRetryConfig {
        public static final int NUM_TIMEOUT_RETRIES = 5;
        public static final int TIMEOUT_RETRY_SLEEP_MILLISECONDS = 2000;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AccountType {
        public static final String CREDITCARD = "CreditCard";
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
                    .ignoreKeys(AccountType.CREDITCARD)
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

    public static final TypeMapper<AccountIdentifierType> PAYMENT_RECIPIENT_TYPE_MAP =
            TypeMapper.<AccountIdentifierType>builder()
                    .put(AccountIdentifierType.SE_BG, "BankGiro", "BankGiroOCR", "BankGiroInvoice")
                    .put(AccountIdentifierType.SE_PG, "PlusGiro", "PlusGiroOCR", "PlusGiroInvoice")
                    .put(
                            AccountIdentifierType.SE,
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

    public static final TypeMapper<AccountCapabilities> ACCOUNT_CAPABILITIES_MAPPER =
            TypeMapper.<AccountCapabilities>builder()
                    .put(
                            new AccountCapabilities(Answer.YES, Answer.YES, Answer.YES, Answer.YES),
                            "AIE",
                            "PayAndDebitCard",
                            "Solvency")
                    .put(
                            new AccountCapabilities(Answer.NO, Answer.YES, Answer.YES, Answer.YES),
                            "Savings")
                    .put(
                            new AccountCapabilities(Answer.NO, Answer.YES, Answer.NO, Answer.YES),
                            "Investeringssparkonto",
                            "SkandiaLivForsakring",
                            "Värdepappersdepå")
                    .put(
                            new AccountCapabilities(Answer.NO, Answer.YES, Answer.NO, Answer.NO),
                            "IPSDepa",
                            "IPS-depå",
                            "Kapitalförsäkringsdepå",
                            "SkandiaLivAiEForsakring")
                    .put(
                            new AccountCapabilities(Answer.NO, Answer.NO, Answer.NO, Answer.NO),
                            "Payment",
                            "TPSWithUnitLink")
                    .build();
}
