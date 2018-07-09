package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher;

import java.util.Collection;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.core.Amount;

public class TargoBankInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {
    public static final String MAX_ELEMENTS = "25";
    private static final Logger LOGGER = LoggerFactory.getLogger(TargoBankInvestmentAccountFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(
            TargoBankInvestmentAccountFetcher.class);
    private final static LogTag investmentLogTag = LogTag.from("targobank_investment_data");
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
                .orElse(apiClient.requestInvestmentAccounts());

        Collection<InvestmentAccount> accountsDetails = Lists.newArrayList();
        AGGREGATION_LOGGER.info(accountsDetails.toString());
        investmentAccountsListResponse.getSecurityAccountList().stream()
                .forEach(a ->
                        {
                            //TODO: Temporary get only one page
                            int page = 1;
                            InvestmentAccountOverviewResponse investmentAccount = apiClient
                                    .requestAccountDetails(a.getNumber(), page);
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

}
