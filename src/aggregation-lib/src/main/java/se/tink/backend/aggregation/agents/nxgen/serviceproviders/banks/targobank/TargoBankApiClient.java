package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.sessionHandler.rpc.InitResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class TargoBankApiClient {
    private final TinkHttpClient client;
    private final TargoBankConfiguration config;

    public TargoBankApiClient(TinkHttpClient client, TargoBankConfiguration config) {
        this.client = client;
        this.config = config;
    }

    private RequestBuilder buildRequestHeaders(String urlString) {
        URL url = new URL(config.getUrl() + urlString);
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE, MediaType.TEXT_HTML_TYPE)
                .header(TargoBankConstants.Headers.CONTENT_TYPE);
    }

    public LoginResponse logon(String logonBody) {
        return buildRequestHeaders(TargoBankConstants.Url.LOGIN)
                .body(logonBody)
                .post(LoginResponse.class);
    }

    public LogoutResponse logout() {
        return buildRequestHeaders(TargoBankConstants.Url.LOGOUT)
                .post(LogoutResponse.class);
    }

    public InvestmentAccountOverviewResponse getInvestmentAccountDetails(String accountSummaryBody) {
        return buildRequestHeaders(TargoBankConstants.Url.INVESTMENT_ACCOUNT)
                .body(accountSummaryBody)
                .post(InvestmentAccountOverviewResponse.class);
    }

    public InvestmentAccountsListResponse getInvestmentAccounts(String accountSummaryBody) {
        return buildRequestHeaders(TargoBankConstants.Url.INVESTMENT_ACCOUNTS)
                .body(accountSummaryBody)
                .post(InvestmentAccountsListResponse.class);
    }

    public AccountSummaryResponse getAccounts(String accountSummaryBody) {
        return buildRequestHeaders(TargoBankConstants.Url.ACCOUNTS)
                .body(accountSummaryBody)
                .post(AccountSummaryResponse.class);
    }

    public TransactionSummaryResponse getTransactions(String body) {
        return buildRequestHeaders(TargoBankConstants.Url.TRANSACTIONS)
                .body(body)
                .post(TransactionSummaryResponse.class);
    }

    //Seems it's not obligatory call so use it for keep-alive
    public InitResponse actionInit(String body) {
        return buildRequestHeaders(TargoBankConstants.Url.INIT)
                .body(body)
                .post(InitResponse.class);
    }
}
