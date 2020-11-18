package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.GroupAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.GroupEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecuritiesRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecuritiesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecurityDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.ListSecurityDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class DanskeBankInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final DanskeBankApiClient apiClient;
    private final DanskeBankConfiguration configuration;

    private Collection<GroupAccountEntity> accounts;

    public DanskeBankInvestmentFetcher(
            DanskeBankApiClient apiClient, DanskeBankConfiguration configuration) {
        this.apiClient = apiClient;
        this.configuration = configuration;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return listAccounts().stream()
                .map(
                        custodyAccount -> {
                            ListSecuritiesResponse listSecurities =
                                    apiClient.listSecurities(
                                            ListSecuritiesRequest.createFromCustodyAccount(
                                                    custodyAccount.getAccountIdentifier()));

                            return custodyAccount.toInvestmentAccount(
                                    listSecurities.getMarketValueCurrency(),
                                    createPortfolio(custodyAccount, listSecurities),
                                    configuration);
                        })
                .collect(Collectors.toList());
    }

    // There is only one portfolio for each account
    private List<Portfolio> createPortfolio(
            GroupAccountEntity custodyAccount, ListSecuritiesResponse listSecurities) {
        Portfolio portfolio = custodyAccount.toTinkPortfolio(listSecurities, configuration);

        List<Instrument> instruments = Lists.newArrayList();
        portfolio.setInstruments(instruments);

        listSecurities.getSecurities().stream()
                .filter(DanskeBankPredicates.NON_ZERO_QUANTITY)
                .forEach(
                        security ->
                                security.toTinkInstrument(
                                                fetchSecurityDetails(security.getId()),
                                                configuration)
                                        .ifPresent(instruments::add));

        return Collections.singletonList(portfolio);
    }

    private Collection<GroupAccountEntity> listAccounts() {
        if (accounts == null) {
            accounts =
                    apiClient.listCustodyAccounts().getGroups().stream()
                            .filter(DanskeBankPredicates.GROUPS_WITH_ACCOUNTS)
                            .map(GroupEntity::getAccounts)
                            .flatMap(List::stream)
                            .filter(DanskeBankPredicates.NON_ALL_INVESTMENTS_GROUP)
                            .collect(Collectors.toList());
        }

        return accounts;
    }

    private ListSecurityDetailsResponse fetchSecurityDetails(String securityId) {
        return apiClient.listSecurityDetails(
                ListSecurityDetailsRequest.createFromSecurityId(securityId));
    }
}
