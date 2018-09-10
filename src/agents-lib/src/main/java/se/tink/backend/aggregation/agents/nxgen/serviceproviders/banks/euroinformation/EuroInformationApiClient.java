package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import java.util.Collections;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LogoutResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.AccountTypeEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountOverviewRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountsListRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.AccountSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.TransactionSummaryRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.InitRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.rpc.InitResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationApiClient {
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(EuroInformationApiClient.class);
    private final static LogTag unknownAccountTypesTag = LogTag.from("euroinformation_unknown_accounts");
    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    protected final EuroInformationConfiguration config;
    protected final Logger LOGGER = LoggerFactory.getLogger(EuroInformationApiClient.class);

    public EuroInformationApiClient(TinkHttpClient client,
            SessionStorage sessionStorage, EuroInformationConfiguration config) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.config = config;
    }

    protected RequestBuilder buildRequestHeaders(String urlString) {
        URL url = new URL(config.getUrl() + urlString);
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_XML_TYPE,
                        MediaType.TEXT_HTML_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }

    public LoginResponse logon(String username, String password) {
        return buildRequestHeaders(config.getLoginSubpage())
                .body(new LoginRequest(username, password, config.getAppVersionKey(), config.getAppVersion(),
                        config.getTarget()))
                .post(LoginResponse.class);
    }

    public LogoutResponse logout() {
        return buildRequestHeaders(EuroInformationConstants.Url.LOGOUT)
                .post(LogoutResponse.class);
    }

    public InvestmentAccountsListResponse requestInvestmentAccounts() {
        InvestmentAccountsListResponse details = buildRequestHeaders(EuroInformationConstants.Url.INVESTMENT_ACCOUNTS)
                .post(InvestmentAccountsListResponse.class, new InvestmentAccountsListRequest(1));
        this.sessionStorage.put(EuroInformationConstants.Tags.INVESTMENT_ACCOUNTS, details);
        return details;
    }

    public InvestmentAccountOverviewResponse requestAccountDetails(String accountNumber, int page) {
        return buildRequestHeaders(EuroInformationConstants.Url.INVESTMENT_ACCOUNT)
                .post(InvestmentAccountOverviewResponse.class,
                        new InvestmentAccountOverviewRequest(page, accountNumber));
    }

    public AccountSummaryResponse requestAccounts() {
        AccountSummaryResponse details = buildRequestHeaders(EuroInformationConstants.Url.ACCOUNTS)
                .post(AccountSummaryResponse.class, new AccountSummaryRequest());
        this.sessionStorage.put(EuroInformationConstants.Tags.ACCOUNT_LIST, details);

        // Print unknown account types
        Optional.ofNullable(details.getAccountDetailsList()).orElseGet(Collections::emptyList).stream()
                .filter(a -> a.getTinkTypeByTypeNumber().equals(AccountTypeEnum.UNKNOWN))
                .forEach(acc -> AGGREGATION_LOGGER
                        .infoExtraLong(SerializationUtils.serializeToString(details), unknownAccountTypesTag));
        return details;
    }

    public TransactionSummaryResponse getTransactions(String webId) {
        return buildRequestHeaders(EuroInformationConstants.Url.TRANSACTIONS)
                .post(TransactionSummaryResponse.class, new TransactionSummaryRequest(webId));
    }

    //Seems it's not obligatory call so use it for keep-alive
    public InitResponse actionInit() {
        return buildRequestHeaders(EuroInformationConstants.Url.INIT)
                .post(InitResponse.class, new InitRequest());
    }

}
