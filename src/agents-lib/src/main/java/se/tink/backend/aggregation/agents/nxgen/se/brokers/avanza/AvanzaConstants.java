package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import java.time.ZoneId;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class AvanzaConstants {
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");

    public static final AvanzaAccountTypeMappers MAPPERS = new AvanzaAccountTypeMappers();

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

    public static class AvanzaAccountTypes {
        public static final String AKTIE_FONDKONTO = "AktieFondkonto";
        public static final String INVESTERINGSSPARKONTO = "Investeringssparkonto";
        public static final String KAPITALFORSAKRING = "Kapitalforsakring";
        public static final String SPARKONTO = "Sparkonto";
        public static final String SPARKONTOPLUS = "SparkontoPlus";
        public static final String TJANSTEPENSION = "Tjanstepension";
        public static final String PENSIONSFORSAKRING = "Pensionsforsakring";
        public static final String IPS = "IPS";
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
    }

    public static class Urls {
        private static final String HOST = "https://www.avanza.se";
        private static final String API = HOST + "/_api";
        private static final String AUTH = API + "/authentication";
        public static final String LOGOUT = AUTH + "/sessions/%s";
        public static final String BANK_ID_INIT = AUTH + "/sessions/bankid";
        public static final String BANK_ID_COLLECT = AUTH + "/sessions/bankid/%s";
        public static final String BANK_ID_COMPLETE =
                AUTH + "/sessions/bankid/%s/%s?maxInactiveMinutes=60";
        private static final String MOBILE = HOST + "/_mobile";
        public static final String MARKET_INFO = MOBILE + "/market/%s/%s";

        private static final String ACCOUNT = MOBILE + "/account";
        public static final String ACCOUNTS_OVERVIEW = ACCOUNT + "/overview";
        public static final String ACCOUNT_DETAILS = ACCOUNT + "/%s/overview";
        public static final String INVESTMENT_PORTFOLIO_POSITIONS =
                ACCOUNT + "/%s/positions?autoPortfolio=1&sort=name";
        private static final String TRANSACTIONS = ACCOUNT + "/transactions";
        public static final String TRANSACTIONS_LIST = TRANSACTIONS + "/%s?from=%s&to=%s";
        public static final String INVESTMENT_TRANSACTIONS_LIST =
                TRANSACTIONS + "/%s/options?from=%s&includeInstrumentsWithNoOrderbook=1&to=%s";
    }

    public static class TransactionTypes {
        public static final String DEPOSIT = "deposit";
        public static final String WITHDRAW = "withdraw";
    }

    public static class StorageKeys {
        public static final String HOLDER_NAME = "holder_name";
    }

    public static class QueryKeys {}

    public static class QueryValues {
        public static final String FROM_DATE_FOR_INVESTMENT_TRANSACTIONS = "2000-01-01";
    }

    public static class HeaderKeys {
        public static final String AUTH_SESSION = "X-AuthenticationSession";
        public static final String SECURITY_TOKEN = "X-SecurityToken";
    }

    public static class FormKeys {}

    public static class FormValues {}

    public static class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("avanza_unknown_accountype");
    }
}
