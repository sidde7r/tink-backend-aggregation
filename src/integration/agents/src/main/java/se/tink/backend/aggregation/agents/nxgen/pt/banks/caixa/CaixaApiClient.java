package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa;

import java.time.LocalDate;
import java.time.YearMonth;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.Parameters;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.CardAccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.CardAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.CardAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.InvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.MarketDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc.PortfolioDetailsResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class CaixaApiClient {

    private final TinkHttpClient httpClient;

    private static final String ENCODED_SPACE = "%20";

    CaixaApiClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void authenticate(String login, String password) {
        createBaseRequest(Urls.AUTH)
                .queryParam(QueryParams.USER, login)
                .addBasicAuth(login, password)
                .post(HttpResponse.class);
    }

    public AccountsResponse fetchAccounts() {
        return createBaseRequest(Urls.FETCH_ACCOUNTS)
                .queryParam(
                        QueryParams.TARGET_OPERATION_TYPE,
                        QueryValues.BALANCES_AND_TRANSACTIONS_OPERATION)
                .get(AccountsResponse.class);
    }

    public InvestmentAccountsResponse fetchInvestmentAccounts() {
        return createBaseRequest(Urls.FETCH_ACCOUNTS)
                .queryParam(
                        QueryParams.TARGET_OPERATION_TYPE,
                        QueryValues.INVESTMENT_PORTFOLIO_OPERATION)
                .get(InvestmentAccountsResponse.class);
    }

    public PortfolioDetailsResponse fetchInvestmentPortfolio(String accountKey) {
        return createBaseRequest(Urls.FETCH_INVESTMENT_DETAILS)
                .queryParam(QueryParams.FULL_ACCOUNT_KEY, accountKey)
                .get(PortfolioDetailsResponse.class);
    }

    public MarketDetailsResponse fetchMarketDetails(String assetTypeId) {
        return createBaseRequest(Urls.FETCH_MARKET_DETAILS)
                .queryParam(QueryParams.ASSET_TYPE_ID, assetTypeId)
                .queryParam(
                        QueryParams.QUOTES_SEARCH_TYPE_ID,
                        QueryValues.PORTFOLIO_MARKET_DETAILS_OPERATION)
                .get(MarketDetailsResponse.class);
    }

    public AccountDetailsResponse fetchBalance(String accountKey) {
        return createBaseRequest(buildFetchAccountDetailsUrl(accountKey))
                .queryParam(QueryParams.INCLUDE_BALANCES, Boolean.TRUE.toString())
                .get(AccountDetailsResponse.class);
    }

    public AccountDetailsResponse fetchTransactions(
            String accountKey, LocalDate from, LocalDate to) {
        return createBaseRequest(buildFetchAccountDetailsUrl(accountKey))
                .queryParam(QueryParams.INCLUDE_TRANSACTIONS, Boolean.TRUE.toString())
                .queryParam(QueryParams.FROM, CaixaConstants.DATE_FORMATTER.format(from))
                .queryParam(QueryParams.TO, CaixaConstants.DATE_FORMATTER.format(to))
                .get(AccountDetailsResponse.class);
    }

    public CardAccountsResponse fetchCardAccounts() {
        return createBaseRequest(Urls.FETCH_CARD_ACCOUNTS)
                .queryParam(
                        QueryParams.TARGET_CARD_OPERATION_TYPE,
                        QueryValues.BALANCES_AND_TRANSACTIONS_OPERATION)
                .get(CardAccountsResponse.class);
    }

    public CardAccountResponse fetchCardAccountDetails(String cardAccountId) {
        return createBaseRequest(Urls.FETCH_CARD_ACCOUNTS)
                .queryParam(
                        QueryParams.TARGET_CARD_OPERATION_TYPE,
                        QueryValues.BALANCES_AND_TRANSACTIONS_OPERATION)
                .queryParam(QueryParams.CARD_ACCOUNT_ID, cardAccountId)
                .get(CardAccountResponse.class);
    }

    public CardAccountDetailsResponse fetchCardAccountTransactions(
            String cardAccountId, YearMonth statementDate) {
        return createBaseRequest(
                        Urls.FETCH_CARD_ACCOUNT_TRANSACTIONS.parameter(
                                Parameters.CARD_ACCOUNT_ID, cardAccountId))
                .queryParam(
                        QueryParams.STATEMENT_DATE,
                        statementDate.format(CaixaConstants.YEARMONTH_FORMATTER))
                .get(CardAccountDetailsResponse.class);
    }

    private URL buildFetchAccountDetailsUrl(String accountKey) {
        return new URL(
                Urls.FETCH_ACCOUNT_DETAILS
                        .parameter(Parameters.ACCOUNT_KEY, accountKey)
                        .get()
                        .replace("+", ENCODED_SPACE));
    }

    private RequestBuilder createBaseRequest(URL url) {
        return httpClient
                .request(url)
                .accept(HeaderValues.ACCEPT)
                .header(HeaderKeys.X_CGD_APP_DEVICE, HeaderValues.DEVICE_NAME)
                .header(HeaderKeys.X_CGD_APP_NAME, HeaderValues.APP_NAME)
                .header(HeaderKeys.X_CGD_APP_VERSION, HeaderValues.APP_VERSION);
    }
}
