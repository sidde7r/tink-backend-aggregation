package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.InstrumentTypes;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdCollectResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitRequest;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc.BankIdInitResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.BondMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.CertificateMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.EquityLinkedBondMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.ExchangeTradedFundInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.FundMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.FutureForwardMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.InvestmentAccountPortfolioResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.InvestmentTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.StockMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc.WarrantMarketInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.AccountsOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class AvanzaApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvanzaApiClient.class);

    private final TinkHttpClient client;
    private final AvanzaAuthSessionStorage authSessionStorage;

    public AvanzaApiClient(TinkHttpClient client, AvanzaAuthSessionStorage authSessionStorage) {
        this.client = client;
        this.authSessionStorage = authSessionStorage;
    }

    private RequestBuilder createRequest(String url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(
            String url, String authSession, String securityToken) {
        return createRequest(url)
                .header(HeaderKeys.AUTH_SESSION, authSession)
                .header(HeaderKeys.SECURITY_TOKEN, securityToken);
    }

    private RequestBuilder createRequestInSession(String url, String authSession) {
        final String securityToken = authSessionStorage.get(authSession);

        return createRequestInSession(url, authSession, securityToken);
    }

    public BankIdInitResponse initBankId(BankIdInitRequest request) {
        return createRequest(Urls.BANK_ID_INIT()).post(BankIdInitResponse.class, request);
    }

    public BankIdCollectResponse collectBankId(String transactionId) {
        final String collectUrl = Urls.BANK_ID_COLLECT(transactionId);

        return createRequest(collectUrl).get(BankIdCollectResponse.class);
    }

    public HttpResponse completeBankId(String transactionId, String customerId) {
        final String completeUrl = Urls.BANK_ID_COMPLETE(transactionId, customerId);

        return createRequest(completeUrl).get(HttpResponse.class);
    }

    public AccountsOverviewResponse fetchAccounts(String authSession) {
        return createRequestInSession(Urls.ACCOUNTS_OVERVIEW(), authSession)
                .get(AccountsOverviewResponse.class);
    }

    public AccountDetailsResponse fetchAccountDetails(String accountId, String authSession) {
        final String detailsUrl = Urls.ACCOUNT_DETAILS(accountId);

        return createRequestInSession(detailsUrl, authSession).get(AccountDetailsResponse.class);
    }

    public TransactionsResponse fetchTransactions(
            String accountId, String fromDate, String toDate, String authSession) {
        final String transactionsUrl = Urls.TRANSACTIONS_LIST(accountId, fromDate, toDate);

        return createRequestInSession(transactionsUrl, authSession).get(TransactionsResponse.class);
    }

    public InvestmentTransactionsResponse fetchInvestmentTransactions(
            String accountId, String toDate, String authSession) {
        final String investmentTransactionsUrl =
                Urls.INVESTMENT_TRANSACTIONS_LIST(
                        accountId, QueryValues.FROM_DATE_FOR_INVESTMENT_TRANSACTIONS, toDate);

        return createRequestInSession(investmentTransactionsUrl, authSession)
                .get(InvestmentTransactionsResponse.class);
    }

    public InvestmentAccountPortfolioResponse fetchInvestmentAccountPortfolio(
            String accountId, String authSession) {
        final String positionsUrl = Urls.INVESTMENT_PORTFOLIO_POSITIONS(accountId);

        return createRequestInSession(positionsUrl, authSession)
                .get(InvestmentAccountPortfolioResponse.class);
    }

    public <T> T fetchMarketInfoResponse(
            String instrumentType, String orderbookId, String authSession, Class<T> responseType) {
        final String marketInfoUrl = Urls.MARKET_INFO(instrumentType, orderbookId);

        return createRequestInSession(marketInfoUrl, authSession).get(responseType);
    }

    public String getInstrumentMarket(
            String instrumentType, String orderbookId, String authSession) {
        if (instrumentType == null) {
            return null;
        }

        String type = instrumentType.toLowerCase();
        switch (type) {
            case InstrumentTypes.AUTO_PORTFOLIO:
            case InstrumentTypes.FUND:
                FundMarketInfoResponse fundMarketInfoResponse =
                        fetchMarketInfoResponse(
                                InstrumentTypes.FUND,
                                orderbookId,
                                authSession,
                                FundMarketInfoResponse.class);
                return fundMarketInfoResponse.getFundCompany() != null
                        ? fundMarketInfoResponse.getFundCompany().getName()
                        : null;
            case InstrumentTypes.STOCK:
                return fetchMarketInfoResponse(
                                type, orderbookId, authSession, StockMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.CERTIFICATE:
                return fetchMarketInfoResponse(
                                type, orderbookId, authSession, CertificateMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.FUTURE_FORWARD:
                return fetchMarketInfoResponse(
                                type,
                                orderbookId,
                                authSession,
                                FutureForwardMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.EQUITY_LINKED_BOND:
                return fetchMarketInfoResponse(
                                type,
                                orderbookId,
                                authSession,
                                EquityLinkedBondMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.BOND:
                return fetchMarketInfoResponse(
                                type, orderbookId, authSession, BondMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.WARRANT:
                return fetchMarketInfoResponse(
                                type, orderbookId, authSession, WarrantMarketInfoResponse.class)
                        .getMarketPlace();
            case InstrumentTypes.EXCHANGE_TRADED_FUND:
                return fetchMarketInfoResponse(
                                type,
                                orderbookId,
                                authSession,
                                ExchangeTradedFundInfoResponse.class)
                        .getMarketPlace();

            default:
                LOGGER.warn(
                        String.format(
                                "avanza - portfolio type not handled in switch - type: %s, orderbookId: %s",
                                instrumentType, orderbookId));
                return null;
        }
    }

    public String logout(String authSession) {
        final String logoutUrl = Urls.LOGOUT(authSession);

        return createRequestInSession(logoutUrl, authSession).delete(String.class);
    }
}
