package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank;

import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.TargoBankInvestmentAccountFetcher.MAX_ELEMENTS;

public class TargoBankApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final TargoBankConfiguration config;
    private final Logger LOGGER = LoggerFactory.getLogger(TargoBankApiClient.class);

    public TargoBankApiClient(TinkHttpClient client,
            SessionStorage sessionStorage, TargoBankConfiguration config) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.config = config;
    }

    private RequestBuilder buildRequestHeaders(String urlString) {
        URL url = new URL(config.getUrl() + urlString);
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE,
                        MediaType.TEXT_HTML_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }

    public LoginResponse logon(String username, String password) {
        return buildRequestHeaders(TargoBankConstants.Url.LOGIN)
                .body(buildBodyLogonRequest(username, password))
                .post(LoginResponse.class);
    }

    private String buildBodyLogonRequest(String username, String password) {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder.addParameter(TargoBankConstants.RequestBodyValues.USER, username)
                    .addParameter(TargoBankConstants.RequestBodyValues.PASSWORD, password)
                    .addParameter(TargoBankConstants.RequestBodyValues.APP_VERSION,
                            TargoBankConstants.RequestBodyValues.APP_VERSION_VALUE)
                    .addParameter(TargoBankConstants.RequestBodyValues.CIBLE,
                            TargoBankConstants.RequestBodyValues.CIBLE_VALUE)
                    .addParameter(TargoBankConstants.RequestBodyValues.WS_VERSION,
                            "2")
                    .addParameter(TargoBankConstants.RequestBodyValues.MEDIA,
                            TargoBankConstants.RequestBodyValues.MEDIA_VALUE)
                    .build().getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
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

    private URIBuilder buildInvestmentAccountsRequest(int page) {
        return new URIBuilder()
                .addParameter(
                        TargoBankConstants.RequestBodyValues.WS_VERSION,
                        "7")
                .addParameter(
                        TargoBankConstants.RequestBodyValues.CATEGORIZE,
                        TargoBankConstants.RequestBodyValues.CATEGORIZE_VALUE)
                .addParameter(
                        TargoBankConstants.RequestBodyValues.CURRENT_PAGE,
                        String.valueOf(page)
                )
                .addParameter(
                        TargoBankConstants.RequestBodyValues.MAX_ELEMENTS,
                        MAX_ELEMENTS
                )
                .addParameter(
                        TargoBankConstants.RequestBodyValues.MEDIA,
                        TargoBankConstants.RequestBodyValues.MEDIA_VALUE);
    }

    public InvestmentAccountsListResponse requestInvestmentAccounts() {
        String body = null;
        try {
            body = buildInvestmentAccountsRequest(1).build()
                    .getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
        InvestmentAccountsListResponse details = getInvestmentAccounts(body);
        this.sessionStorage.put(TargoBankConstants.Tags.INVESTMENT_ACCOUNTS, details);
        return details;
    }

    public InvestmentAccountOverviewResponse requestAccountDetails(String accountNumber, int page) {
        String body = null;
        try {
            body = buildInvestmentAccountsRequest(page)
                    .addParameter("SecurityAccount", accountNumber)
                    .build().getQuery();

        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
        InvestmentAccountOverviewResponse details = getInvestmentAccountDetails(body);
        return details;
    }

    public InvestmentAccountsListResponse getInvestmentAccounts(String accountSummaryBody) {
        return buildRequestHeaders(TargoBankConstants.Url.INVESTMENT_ACCOUNTS)
                .body(accountSummaryBody)
                .post(InvestmentAccountsListResponse.class);
    }

    public AccountSummaryResponse requestAccounts() {
        AccountSummaryResponse details = buildRequestHeaders(TargoBankConstants.Url.ACCOUNTS)
                .body(buildAccountsSummaryRequest())
                .post(AccountSummaryResponse.class);
        this.sessionStorage.put(TargoBankConstants.Tags.ACCOUNT_LIST, details);
        return details;
    }

    private String buildAccountsSummaryRequest() {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.WS_VERSION,
                            "2")
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.CATEGORIZE,
                            TargoBankConstants.RequestBodyValues.CATEGORIZE_VALUE)
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.MEDIA,
                            TargoBankConstants.RequestBodyValues.MEDIA_VALUE)
                    .build()
                    .getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
    }

    public TransactionSummaryResponse getTransactions(String webId) {
        return buildRequestHeaders(TargoBankConstants.Url.TRANSACTIONS)
                .body(buildTransactionSummaryRequest(webId))
                .post(TransactionSummaryResponse.class);
    }

    private String buildTransactionSummaryRequest(String webId) {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.WEB_ID,
                            webId)
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.WS_VERSION,
                            "1")
                    .addParameter(
                            TargoBankConstants.RequestBodyValues.MEDIA,
                            TargoBankConstants.RequestBodyValues.MEDIA_VALUE)
                    .build()
                    .getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
    }

    //Seems it's not obligatory call so use it for keep-alive
    public InitResponse actionInit(String body) {
        return buildRequestHeaders(TargoBankConstants.Url.INIT)
                .body(body)
                .post(InitResponse.class);
    }

}
