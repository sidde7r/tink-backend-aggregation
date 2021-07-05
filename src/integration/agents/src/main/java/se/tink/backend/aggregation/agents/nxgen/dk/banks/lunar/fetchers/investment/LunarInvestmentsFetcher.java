package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.LogTags.LUNAR_TAG;

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
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.identity.LunarIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.investment.rpc.InvestmentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class LunarInvestmentsFetcher implements AccountFetcher<InvestmentAccount> {

    private final FetcherApiClient apiClient;
    private final LunarIdentityDataFetcher identityDataFetcher;

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            return Optional.ofNullable(apiClient.fetchInvestments())
                    .map(InvestmentsResponse::getPortfolio)
                    .filter(this::hasAccountNumber)
                    .map(this::getInvestmentAccounts)
                    .orElse(Collections.emptyList());
        } catch (HttpResponseException e) {
            // Some users receive 404 response on portfolio. These users probably don't have an
            // investment account
            if (e.getResponse().getStatus() == 404) {
                log.info("{} Received 404 error while fetching investment accounts", LUNAR_TAG);
                return Collections.emptyList();
            }
            throw e;
        }
    }

    private boolean hasAccountNumber(PortfolioEntity portfolioEntity) {
        return StringUtils.isNotBlank(portfolioEntity.getAccountNumber());
    }

    private List<InvestmentAccount> getInvestmentAccounts(PortfolioEntity portfolio) {
        return portfolio.toInvestmentAccounts(
                apiClient.fetchPerformanceData().getPerformanceData(),
                getUserLunarInstruments(),
                identityDataFetcher.getAccountHolder());
    }

    private List<InstrumentEntity> getUserLunarInstruments() {
        return apiClient.fetchInstruments().getInstruments().stream()
                .filter(instrumentEntity -> instrumentEntity.getPosition() != null)
                .filter(instrumentEntity -> BooleanUtils.isNotTrue(instrumentEntity.getDeleted()))
                .filter(
                        instrumentEntity ->
                                instrumentEntity.getCurrentPriceInBaseCurrency() != null)
                .collect(Collectors.toList());
    }
}
