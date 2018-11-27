package se.tink.backend.aggregation.agents.brokers.avanza;

public class AvanzaV2Constants {
    public static final String BASE_URL = "https://www.avanza.se";
    public static final String URL_MARKET_INFO = BASE_URL + "/_mobile/market/%s/%s";
    public static final String URL_TRANSACTIONS =
            BASE_URL + "/_mobile/account/transactions/%s?from=%s&to=%s";
    public static final String URL_POSITIONS =
            BASE_URL + "/_mobile/account/%s/positions?autoPortfolio=1&sort=name";
    public static final String URL_BANK_ID_INIT = BASE_URL + "/_api/authentication/sessions/bankid";
    public static final String URL_BANK_ID_COLLECT =
            BASE_URL + "/_api/authentication/sessions/bankid/%s";
    public static final String URL_INVESTMENT_TRANSACTIONS =
            BASE_URL
                    + "/_mobile/account/transactions/%s/options?from=%s&includeInstrumentsWithNoOrderbook=1&to=%s";
    public static final String URL_ACCOUNT_DETAILS = BASE_URL + "/_mobile/account/%s/overview";
    public static final String URL_ACCOUNT_OVERVIEW = BASE_URL + "/_mobile/account/overview";
    public static final String AUTHENTICATION_SESSION_HEADER = "X-AuthenticationSession";
    public static final String AUTHENTICATION_SESSION_PAYLOAD = "authenticationSession";
    public static final String SECURITY_TOKEN_HEADER = "X-SecurityToken";
    public static final String FROM_DATE_FOR_INVESTMENT_TRANSACTIONS = "2000-01-01";
}
