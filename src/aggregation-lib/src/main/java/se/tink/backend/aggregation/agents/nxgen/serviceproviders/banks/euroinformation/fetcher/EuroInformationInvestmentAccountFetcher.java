package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.SecurityAccountListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.core.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {
    public static final String MAX_ELEMENTS = "25";
    private static final Logger LOGGER = LoggerFactory.getLogger(EuroInformationInvestmentAccountFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(
            EuroInformationInvestmentAccountFetcher.class);
    private final static LogTag investmentLogTag = LogTag.from("euroinformation_investment_data");
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

        if (!EuroInformationUtils.isSuccess(investmentAccountsListResponse.getReturnCode())) {
            return Collections.emptyList();
        }

        List<SecurityAccountListEntity> accountListEntities = Optional
                .ofNullable(investmentAccountsListResponse.getSecurityAccountList()).orElseThrow(() ->
                        new IllegalStateException(
                                SerializationUtils.serializeToString(investmentAccountsListResponse)));

        Collection<InvestmentAccount> accountsDetails = Lists.newArrayList();
        accountListEntities.stream()
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
