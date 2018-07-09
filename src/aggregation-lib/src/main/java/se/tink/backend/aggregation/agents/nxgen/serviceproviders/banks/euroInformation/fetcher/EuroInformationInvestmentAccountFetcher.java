package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.fetcher;

import java.util.Collection;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.fetcher.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.fetcher.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroInformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.core.Amount;

public class EuroInformationInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {
    public static final String MAX_ELEMENTS = "25";
    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationInvestmentAccountFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(
            EuroInformationInvestmentAccountFetcher.class);
    private final static LogTag investmentLogTag = LogTag.from("targobank_investment_data");
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationInvestmentAccountFetcher(EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationInvestmentAccountFetcher create(EuroInformationApiClient apiClient,
            SessionStorage sessionStorage) {
        return new EuroInformationInvestmentAccountFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        InvestmentAccountsListResponse investmentAccountsListResponse = this.sessionStorage
                .get(EuroInformationConstants.Tags.INVESTMENT_ACCOUNTS, InvestmentAccountsListResponse.class)
                .orElseGet(() -> apiClient.requestInvestmentAccounts());

        Collection<InvestmentAccount> accountsDetails = Lists.newArrayList();
        AGGREGATION_LOGGER.info(accountsDetails.toString());
        investmentAccountsListResponse.getSecurityAccountList().stream()
                .forEach(a ->
                        {
                            //TODO: Temporary get only one page
                            int page = 1;
                            InvestmentAccountOverviewResponse investmentAccount = apiClient
                                    .requestAccountDetails(a.getNumber(), page);
                            Amount amount = EuroInformationUtils
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
