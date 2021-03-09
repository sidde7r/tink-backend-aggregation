package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.InvestmentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@Slf4j
@RequiredArgsConstructor
public class LunarInvestmentsFetcher implements AccountFetcher<InvestmentAccount> {

    private final FetcherApiClient apiClient;

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        // Wiski change try catch after getting more data

        try {
            return Optional.ofNullable(apiClient.fetchInvestments())
                    .map(InvestmentsResponse::getPortfolio)
                    .filter(this::hasAccountNumber)
                    .map(this::getInvestmentAccounts)
                    .orElse(Collections.emptyList());
        } catch (RuntimeException e) {
            log.warn("Failed to fetch Lunar investments!", e);
            return Collections.emptyList();
        }
    }

    private boolean hasAccountNumber(PortfolioEntity portfolioEntity) {
        return StringUtils.isNotBlank(portfolioEntity.getAccountNumber());
    }

    private List<InvestmentAccount> getInvestmentAccounts(PortfolioEntity portfolio) {
        log.info("Lunar user has investment account");
        return portfolio.toInvestmentAccounts(
                apiClient.fetchPerformanceData().getPerformanceData(), getUserLunarInstruments());
    }

    private List<InstrumentEntity> getUserLunarInstruments() {
        return apiClient.fetchInstruments().getInstruments().stream()
                .filter(instrumentEntity -> instrumentEntity.getPosition() != null)
                .filter(instrumentEntity -> BooleanUtils.isNotTrue(instrumentEntity.getDeleted()))
                .collect(Collectors.toList());
    }
}
