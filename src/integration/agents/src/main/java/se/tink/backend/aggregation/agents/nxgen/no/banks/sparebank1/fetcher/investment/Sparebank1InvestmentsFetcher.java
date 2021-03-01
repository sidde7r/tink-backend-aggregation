package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@Slf4j
@RequiredArgsConstructor
public class Sparebank1InvestmentsFetcher implements AccountFetcher<InvestmentAccount> {
    private final Sparebank1ApiClient apiClient;

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        // Unfortunately there is no id of investment account, just list of portfolios in response.
        // Processing account deduplication with randomly chosen id is a mystery. Therefore each
        // portfolito is treated as seperate investment account
        List<PortfolioEntity> portfolios = apiClient.fetchInvestments().getPortfolios();
        if (portfolios.size() > 1) {
            log.info("Available multiple portfolios in investment account");
        }
        return portfolios.stream()
                .map(PortfolioEntity::toInvestmentAccount)
                .collect(Collectors.toList());
    }
}
