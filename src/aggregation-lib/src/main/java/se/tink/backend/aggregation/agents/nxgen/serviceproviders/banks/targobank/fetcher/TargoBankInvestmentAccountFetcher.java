package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher;

import java.net.URISyntaxException;
import java.util.Collection;
import org.apache.http.client.utils.URIBuilder;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.core.Amount;

public class TargoBankInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TargoBankInvestmentAccountFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(
            TargoBankInvestmentAccountFetcher.class);
    private final static LogTag investmentLogTag = LogTag.from("targobank_investment_data");
    public static final String MAX_ELEMENTS = "25";
    private final TargoBankApiClient apiClient;
    private final SessionStorage sessionStorage;

    private TargoBankInvestmentAccountFetcher(TargoBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static TargoBankInvestmentAccountFetcher create(TargoBankApiClient apiClient,
            SessionStorage sessionStorage) {
        return new TargoBankInvestmentAccountFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {

        InvestmentAccountsListResponse investmentAccountsListResponse = this.sessionStorage
                .get(TargoBankConstants.Tags.INVESTMENT_ACCOUNTS, InvestmentAccountsListResponse.class)
                .orElse(requestAccounts());

        Collection<InvestmentAccount> accountsDetails = Lists.newArrayList();
        AGGREGATION_LOGGER.info(accountsDetails.toString());
        investmentAccountsListResponse.getSecurityAccountList().stream()
                .forEach(a ->
                        {
                            //TODO: Temporary get only one page
                            int page = 1;
                            InvestmentAccountOverviewResponse investmentAccount = requestAccountDetails(a.getNumber(), page);
                            Amount amount = TargoBankUtils
                                    .parseAmount(investmentAccount.getSecurityAccountOverview().getOverview().getAmount());
                            AGGREGATION_LOGGER.infoExtraLong(investmentAccount.toString(), investmentLogTag);
                            accountsDetails.add(InvestmentAccount
                                    .builder(investmentAccount.getSecurityAccountOverview().getOverview().getNumber(), amount)
                                    .build());
                        }
                );
        return accountsDetails;
    }

    private InvestmentAccountsListResponse requestAccounts() {
        String body = null;
        try {
            body = buildInvestmentAccountsRequest(1).build()
                    .getQuery();
        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
        InvestmentAccountsListResponse details = apiClient.getInvestmentAccounts(body);
        this.sessionStorage.put(TargoBankConstants.Tags.INVESTMENT_ACCOUNTS, details);
        return details;
    }

    private InvestmentAccountOverviewResponse requestAccountDetails(String accountNumber, int page) {
        String body = null;
        try {
            body = buildInvestmentAccountsRequest(page)
                    .addParameter("SecurityAccount", accountNumber)
                    .build().getQuery();

        } catch (URISyntaxException e) {
            LOGGER.error("Error building login body request\n", e);
            throw new RuntimeException(e);
        }
        InvestmentAccountOverviewResponse details = apiClient.getInvestmentAccountDetails(body);
        this.sessionStorage.put(TargoBankConstants.Tags.INVESTMENT_ACCOUNT, details);

        return details;
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
}
