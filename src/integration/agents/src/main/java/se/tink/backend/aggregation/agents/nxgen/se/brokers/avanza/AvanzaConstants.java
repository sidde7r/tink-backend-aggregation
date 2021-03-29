package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import com.sun.jersey.api.uri.UriTemplate;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class AvanzaConstants {

    public static final AvanzaAccountTypeMappers MAPPERS = new AvanzaAccountTypeMappers();
    public static final String CLUSTER_ID_NESTON = "neston";

    public enum BankIdResponseStatus {
        NO_CLIENT("NO_CLIENT"),
        USER_SIGN("USER_SIGN"),
        STARTED("STARTED"),
        COMPLETE("COMPLETE"),
        CANCELLED("CANCELLED"),
        TIMEOUT("TIMEOUT"),
        ALREADY_IN_PROGRESS("OUTSTANDING_TRANSACTION"),
        UNKNOWN("");

        private String statusCode;

        BankIdResponseStatus(String statusCode) {
            this.statusCode = statusCode;
        }

        public static BankIdResponseStatus fromStatusCode(String statusCode) {
            return Arrays.stream(BankIdResponseStatus.values())
                    .filter(status -> status.getStatusCode().equalsIgnoreCase(statusCode))
                    .findFirst()
                    .orElse(BankIdResponseStatus.UNKNOWN);
        }

        public String getStatusCode() {
            return statusCode;
        }
    }

    public static class Currencies {
        public static final String SEK = "SEK";
    }

    public static class AvanzaAccountTypes {
        public static final String AKTIE_FONDKONTO = "AktieFondkonto";
        public static final String INVESTERINGSSPARKONTO = "Investeringssparkonto";
        public static final String KAPITALFORSAKRING = "Kapitalforsakring";
        public static final String KAPITALFORSAKRING_BARN = "KapitalforsakringBarn";
        public static final String SPARKONTO = "Sparkonto";
        public static final String SPARKONTOPLUS = "SparkontoPlus";
        public static final String TJANSTEPENSION = "Tjanstepension";
        public static final String PENSIONSFORSAKRING = "Pensionsforsakring";
        public static final String IPS = "IPS";
        public static final String KREDITKONTO_ISK = "KreditkontoISK";
        public static final String AVTALS_PENSION = "AvtalsPension";
        public static final String SUPER_BOLANEKONTO = "Superbolanekonto";
        public static final String KREDITKONTO_KF = "KreditkontoKF";
    }

    public static class AvanzaFallbackAccountTypes {
        public static final String PENSION = "pension";
        public static final String SPARKONTO = "sparkonto";
        public static final String KREDIT = "kredit";
    }

    public static class InstrumentTypes {
        public static final String AUTO_PORTFOLIO = "auto_portfolio";
        public static final String BOND = "bond";
        public static final String CERTIFICATE = "certificate";
        public static final String CONVERTIBLE = "convertible";
        public static final String EQUITY_LINKED_BOND = "equity_linked_bond";
        public static final String EXCHANGE_TRADED_FUND = "exchange_traded_fund";
        public static final String FUND = "fund";
        public static final String FUTURE_FORWARD = "future_forward";
        public static final String INDEX = "index";
        public static final String OPTION = "option";
        public static final String PREMIUM_BOND = "premium_bond";
        public static final String STOCK = "stock";
        public static final String SUBSCRIPTION_OPTION = "subscription_option";
        public static final String WARRANT = "warrant";
    }

    public static class PortfolioTypes {
        public static final String INVESTERINGSSPARKONTO = "investeringssparkonto";
        public static final String AKTIEFONDKONTO = "aktiefondkonto";
        public static final String TJANSTEPENSION = "tjanstepension";
        public static final String PENSIONSFORSAKRING = "pensionsforsakring";
        public static final String IPS = "ips";
        public static final String KAPITALFORSAKRING = "kapitalforsakring";
        public static final String KAPITALFORSAKRING_BARN = "kapitalforsakringbarn";
        public static final String AVTALS_PENSION = "avtalspension";
    }

    public static class Urls {
        private static final String HOST = "https://www.avanza.se";
        private static final String API = HOST + "/_api";
        private static final String AUTH = API + "/authentication";

        private static final String LOGOUT = AUTH + "/sessions/{authSession}";
        private static final String BANK_ID_INIT = AUTH + "/sessions/bankid";
        private static final String BANK_ID_COLLECT = AUTH + "/sessions/bankid/{transactionId}";
        private static final String BANK_ID_COMPLETE =
                AUTH + "/sessions/bankid/{transactionId}/{customerId}?maxInactiveMinutes=60";
        private static final String MOBILE = HOST + "/_mobile";
        private static final String MARKET_INFO = MOBILE + "/market/{instrumentType}/{orderbookId}";

        private static final String ACCOUNT = MOBILE + "/account";
        private static final String ACCOUNTS_OVERVIEW = ACCOUNT + "/overview";
        private static final String ACCOUNT_DETAILS = ACCOUNT + "/{accountId}/overview";
        private static final String INVESTMENT_PORTFOLIO_POSITIONS =
                ACCOUNT + "/{accountId}/positions?autoPortfolio=1&sort=name";
        private static final String TRANSACTIONS = ACCOUNT + "/transactions";
        private static final String TRANSACTIONS_LIST =
                TRANSACTIONS + "/{accountId}?from={fromDate}&to={toDate}";
        private static final String INVESTMENT_TRANSACTIONS_LIST =
                TRANSACTIONS
                        + "/{accountId}/options?from={fromDate}&includeInstrumentsWithNoOrderbook=1&to={toDate}";

        public static String logout(String authSession) {
            return new UriTemplate(LOGOUT).createURI(authSession);
        }

        public static String bankIdInit() {
            return new UriTemplate(BANK_ID_INIT).createURI();
        }

        public static String bankIdCollect(String transactionId) {
            return new UriTemplate(BANK_ID_COLLECT).createURI(transactionId);
        }

        public static String bankIdComplete(String transactionId, String customerId) {
            return new UriTemplate(BANK_ID_COMPLETE).createURI(transactionId, customerId);
        }

        public static String marketInfo(String instrumentType, String orderbookId) {
            return new UriTemplate(MARKET_INFO).createURI(instrumentType, orderbookId);
        }

        public static String accountsOverview() {
            return new UriTemplate(ACCOUNTS_OVERVIEW).createURI();
        }

        public static String accountDetails(String accountId) {
            return new UriTemplate(ACCOUNT_DETAILS).createURI(accountId);
        }

        public static String investmentPortfolioPositions(String accountId) {
            return new UriTemplate(INVESTMENT_PORTFOLIO_POSITIONS).createURI(accountId);
        }

        public static String transactionsList(String accountId, String fromDate, String toDate) {
            return new UriTemplate(TRANSACTIONS_LIST).createURI(accountId, fromDate, toDate);
        }

        public static String investmentTransactionsList(
                String accountId, String fromDate, String toDate) {
            return new UriTemplate(INVESTMENT_TRANSACTIONS_LIST)
                    .createURI(accountId, fromDate, toDate);
        }
    }

    public static class TransactionTypes {
        public static final String DEPOSIT = "deposit";
        public static final String WITHDRAW = "withdraw";
    }

    public static class StorageKeys {
        public static final String HOLDER_NAME = "holder_name";
        public static final String AUTH_SESSION_FORMAT = "auth_session:%s";
        public static final String SECURITY_TOKEN_FORMAT = "security_token:%s";
        public static final String BANKID_TRANSACTION_ID = "bankid_transaction_id";
    }

    public static class QueryValues {
        public static final String FROM_DATE_FOR_INVESTMENT_TRANSACTIONS = "2000-01-01";
    }

    public static class HeaderKeys {
        public static final String AUTH_SESSION = "X-AuthenticationSession";
        public static final String SECURITY_TOKEN = "X-SecurityToken";
    }

    public static class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("avanza_unknown_accountype");
    }

    public static class AuthError {
        public static final String USER_CANCEL = "user_cancel";
        public static final String CANCELLED = "cancelled";
        public static final String TIMEOUT = "expired_transaction";
    }
}
