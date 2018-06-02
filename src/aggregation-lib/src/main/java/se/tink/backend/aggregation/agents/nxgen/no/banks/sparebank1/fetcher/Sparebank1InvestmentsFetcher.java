package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.HoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.PortfolioEntitiesResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;


public class Sparebank1InvestmentsFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger log = new AggregationLogger(Sparebank1InvestmentsFetcher.class);

    private final Sparebank1ApiClient apiClient;
    private final Credentials credentials;
    private final String bankName;

    public Sparebank1InvestmentsFetcher(Sparebank1ApiClient apiClient, Credentials credentials,
            String bankKey) {
        this.apiClient = apiClient;
        this.credentials = credentials;
        this.bankName = bankKey.substring(4);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        URL portfoliosUrl = Sparebank1Constants.Urls.PORTFOLIOS.parameter(Sparebank1Constants.UrlParameter.BANK_NAME, bankName);
        PortfolioEntitiesResponse portfolioEntitiesResponse = apiClient.get(portfoliosUrl, PortfolioEntitiesResponse.class);
        List<PortfolioEntity> portfolioEntitiesList = portfolioEntitiesResponse.getPortfolios();

        if (portfolioEntitiesList.isEmpty()) {
            return Collections.emptyList();
        }

        return portfolioEntitiesList.stream()
                .map(portfolioEntity -> {
                    Portfolio portfolio = portfolioEntity.toPortfolio();
                    portfolio.setInstruments(fetchInstruments(portfolioEntity));

                    return portfolioEntity.toAccount(portfolio);
                }).collect(Collectors.toList());
    }

    private List<Instrument> fetchInstruments(PortfolioEntity portfolioEntity) {
        List<Instrument> instruments = Lists.newArrayList();

        if (!portfolioEntity.getLinks().containsKey(Sparebank1Constants.PORTFOLIO_HOLDINGS_KEY)) {
            log.warn("Sparebank 1 - link to portfolio holdings is not present.");
            return Collections.emptyList();
        }

        String portfolioHoldingsUrl = portfolioEntity.getLinks()
                .get(Sparebank1Constants.PORTFOLIO_HOLDINGS_KEY).getHref();
        HoldingsResponse holdingsResponse = apiClient.get(portfolioHoldingsUrl, HoldingsResponse.class);

        holdingsResponse.getPortfolioHoldings()
                .forEach(holdingsEntity -> holdingsEntity.toInstrument().ifPresent(instruments::add));

        return instruments;
    }
}
