package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountOverviewRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountsListRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.notpaginated.TransactionSummaryRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.notpaginated.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.paginated.OperationSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.PfmInitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.PfmInitResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EuroInformationApiClient {
    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    protected final EuroInformationConfiguration config;

    public EuroInformationApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            EuroInformationConfiguration config) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.config = config;
    }

    protected RequestBuilder buildRequestHeaders(String urlString) {
        URL url = new URL(config.getUrl() + urlString);
        return client.request(url)
                .accept(
                        MediaType.APPLICATION_JSON_TYPE,
                        MediaType.APPLICATION_XML_TYPE,
                        MediaType.TEXT_HTML_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }

    public LoginResponse logon(String username, String password) {
        return buildRequestHeaders(config.getLoginSubpage())
                .body(
                        new LoginRequest(
                                username,
                                password,
                                config.getAppVersionKey(),
                                config.getAppVersion(),
                                config.getTarget()))
                .post(LoginResponse.class);
    }

    public LogoutResponse logout() {
        return buildRequestHeaders(EuroInformationConstants.Url.LOGOUT).post(LogoutResponse.class);
    }

    public InvestmentAccountsListResponse requestInvestmentAccounts() {
        InvestmentAccountsListResponse details =
                buildRequestHeaders(EuroInformationConstants.Url.INVESTMENT_ACCOUNTS)
                        .post(
                                InvestmentAccountsListResponse.class,
                                new InvestmentAccountsListRequest(1));
        this.sessionStorage.put(EuroInformationConstants.Tags.INVESTMENT_ACCOUNTS, details);
        return details;
    }

    public InvestmentAccountOverviewResponse requestAccountDetails(String accountNumber, int page) {
        return buildRequestHeaders(EuroInformationConstants.Url.INVESTMENT_ACCOUNT)
                .post(
                        InvestmentAccountOverviewResponse.class,
                        new InvestmentAccountOverviewRequest(page, accountNumber));
    }

    public AccountSummaryResponse requestAccounts() {
        AccountSummaryResponse details =
                buildRequestHeaders(EuroInformationConstants.Url.ACCOUNTS)
                        .post(AccountSummaryResponse.class, new AccountSummaryRequest());

        this.sessionStorage.put(EuroInformationConstants.Tags.ACCOUNT_LIST, details);

        return details;
    }

    public TransactionSummaryResponse getTransactionsWhenNoPfm(String webId) {
        return buildRequestHeaders(EuroInformationConstants.Url.TRANSACTIONS_NOT_PAGINATED)
                .post(TransactionSummaryResponse.class, new TransactionSummaryRequest(webId));
    }

    public OperationSummaryResponse getTransactionsWithPfm(String webId, String recoveryKey) {
        return buildRequestHeaders(EuroInformationConstants.Url.TRANSACTIONS_PAGINATED)
                .post(
                        OperationSummaryResponse.class,
                        new TransactionSummaryRequest(webId, recoveryKey));
    }

    public PfmInitResponse actionInit(String endpoint) {
        return buildRequestHeaders(endpoint).post(PfmInitResponse.class, new PfmInitRequest());
    }
}
