package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.InitResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.EuroInformationInvestmentAccountFetcher.MAX_ELEMENTS;

public class EuroInformationApiClient {
    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final EuroInformationConfiguration config;
    private final Logger LOGGER = LoggerFactory.getLogger(EuroInformationApiClient.class);

    public EuroInformationApiClient(TinkHttpClient client,
            SessionStorage sessionStorage, EuroInformationConfiguration config) {
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
        return buildRequestHeaders(EuroInformationConstants.Url.LOGIN)
                .body(buildBodyLogonRequest(username, password))
                .post(LoginResponse.class);
    }

    private String buildBodyLogonRequest(String username, String password) {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder.addParameter(EuroInformationConstants.RequestBodyValues.USER, username)
                    .addParameter(EuroInformationConstants.RequestBodyValues.PASSWORD, password)
                    .addParameter(EuroInformationConstants.RequestBodyValues.APP_VERSION,
                            EuroInformationConstants.RequestBodyValues.APP_VERSION_VALUE)
                    .addParameter(EuroInformationConstants.RequestBodyValues.TARGET,
                            config.getTarget())
                    .addParameter(EuroInformationConstants.RequestBodyValues.WS_VERSION,
                            "2")
                    .addParameter(EuroInformationConstants.RequestBodyValues.MEDIA,
                            EuroInformationConstants.RequestBodyValues.MEDIA_VALUE)
                    .build().getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
    }

    public LogoutResponse logout() {
        return buildRequestHeaders(EuroInformationConstants.Url.LOGOUT)
                .post(LogoutResponse.class);
    }

    public InvestmentAccountOverviewResponse getInvestmentAccountDetails(String accountSummaryBody) {
        return buildRequestHeaders(EuroInformationConstants.Url.INVESTMENT_ACCOUNT)
                .body(accountSummaryBody)
                .post(InvestmentAccountOverviewResponse.class);
    }

    private URIBuilder buildInvestmentAccountsRequest(int page) {
        return new URIBuilder()
                .addParameter(
                        EuroInformationConstants.RequestBodyValues.WS_VERSION,
                        "7")
                .addParameter(
                        EuroInformationConstants.RequestBodyValues.CATEGORIZE,
                        EuroInformationConstants.RequestBodyValues.CATEGORIZE_VALUE)
                .addParameter(
                        EuroInformationConstants.RequestBodyValues.CURRENT_PAGE,
                        String.valueOf(page)
                )
                .addParameter(
                        EuroInformationConstants.RequestBodyValues.MAX_ELEMENTS,
                        MAX_ELEMENTS
                )
                .addParameter(
                        EuroInformationConstants.RequestBodyValues.MEDIA,
                        EuroInformationConstants.RequestBodyValues.MEDIA_VALUE);
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
        this.sessionStorage.put(EuroInformationConstants.Tags.INVESTMENT_ACCOUNTS, details);
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
        return buildRequestHeaders(EuroInformationConstants.Url.INVESTMENT_ACCOUNTS)
                .body(accountSummaryBody)
                .post(InvestmentAccountsListResponse.class);
    }

    public AccountSummaryResponse requestAccounts() {
        AccountSummaryResponse details = buildRequestHeaders(EuroInformationConstants.Url.ACCOUNTS)
                .body(buildAccountsSummaryRequest())
                .post(AccountSummaryResponse.class);
        this.sessionStorage.put(EuroInformationConstants.Tags.ACCOUNT_LIST, details);
        return details;
    }

    private String buildAccountsSummaryRequest() {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder
                    .addParameter(
                            EuroInformationConstants.RequestBodyValues.WS_VERSION,
                            "2")
                    .addParameter(
                            EuroInformationConstants.RequestBodyValues.CATEGORIZE,
                            EuroInformationConstants.RequestBodyValues.CATEGORIZE_VALUE)
                    .addParameter(
                            EuroInformationConstants.RequestBodyValues.MEDIA,
                            EuroInformationConstants.RequestBodyValues.MEDIA_VALUE)
                    .build()
                    .getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
    }

    public TransactionSummaryResponse getTransactions(String webId) {
        return buildRequestHeaders(EuroInformationConstants.Url.TRANSACTIONS)
                .body(buildTransactionSummaryRequest(webId))
                .post(TransactionSummaryResponse.class);
    }

    private String buildTransactionSummaryRequest(String webId) {
        URIBuilder uriBuilder = new URIBuilder();
        try {
            return uriBuilder
                    .addParameter(
                            EuroInformationConstants.RequestBodyValues.WEB_ID,
                            webId)
                    .addParameter(
                            EuroInformationConstants.RequestBodyValues.WS_VERSION,
                            "1")
                    .addParameter(
                            EuroInformationConstants.RequestBodyValues.MEDIA,
                            EuroInformationConstants.RequestBodyValues.MEDIA_VALUE)
                    .build()
                    .getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
    }

    //Seems it's not obligatory call so use it for keep-alive
    public InitResponse actionInit(String body) {
        return buildRequestHeaders(EuroInformationConstants.Url.INIT)
                .body(body)
                .post(InitResponse.class);
    }

}
