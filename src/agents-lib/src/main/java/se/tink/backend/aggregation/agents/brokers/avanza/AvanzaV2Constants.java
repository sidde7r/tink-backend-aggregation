package se.tink.backend.aggregation.agents.brokers.avanza;

import se.tink.backend.aggregation.agents.utils.log.LogTag;

public class AvanzaV2Constants {
    public static final AvanzaV2AccountTypeMappers MAPPERS = new AvanzaV2AccountTypeMappers();

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

    public static class Urls {
        public static final String HOST = "https://www.avanza.se";

        private static final String API = HOST + "/_api";
        private static final String AUTH = API + "/authentication";
        private static final String MOBILE = HOST + "/_mobile";
        private static final String ACCOUNT = MOBILE + "/account";
        private static final String TRANSACTIONS = ACCOUNT + "/transactions";

        public static final String LOGOUT = AUTH + "/sessions/%s";
        public static final String BANK_ID_INIT = AUTH + "/sessions/bankid";
        public static final String BANK_ID_COLLECT = AUTH + "/sessions/bankid/%s";
        public static final String BANK_ID_COMPLETE =
                AUTH + "/sessions/bankid/%s/%s?maxInactiveMinutes=60";

        public static final String ACCOUNTS_OVERVIEW = ACCOUNT + "/overview";
        public static final String ACCOUNT_DETAILS = ACCOUNT + "/%s/overview";
        public static final String ACCOUNT_TRANSACTIONS = TRANSACTIONS + "/%s?from=%s&to=%s";

        public static final String INVESTMENT_TRANSACTIONS =
                TRANSACTIONS + "/%s/options?from=%s&includeInstrumentsWithNoOrderbook=1&to=%s";
        public static final String INVESTMENT_POSITIONS =
                ACCOUNT + "/%s/positions?autoPortfolio=1&sort=name";

        public static final String MARKET_INFO = MOBILE + "/market/%s/%s";
    }

    public static class Headers {
        public static final String AUTHENTICATION_SESSION = "X-AuthenticationSession";
        public static final String SECURITY_TOKEN = "X-SecurityToken";
    }

    public static class QueryParams {
        public static final String FROM_DATE_FOR_INVESTMENT_TRANSACTIONS = "2000-01-01";
    }

    public static class Payloads {
        public static final String AUTHENTICATION_SESSION_PAYLOAD = "authenticationSession";
    }

    public static class LogTags {
        public static final LogTag UNKNOWN_ACCOUNT_TYPE = LogTag.from("avanza_unknown_accountype");
    }
}
