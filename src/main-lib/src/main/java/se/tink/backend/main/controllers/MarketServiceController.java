package se.tink.backend.main.controllers;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.common.providers.MarketProvider;
import se.tink.backend.core.Market;
import se.tink.backend.core.MarketStatus;

/***
 * Controller, which contains business logic for work with markets.
 */
// TODO: create only one instance of this class
public class MarketServiceController {
    private final MarketProvider marketProvider;

    @Inject
    public MarketServiceController(MarketProvider marketProvider) {
        this.marketProvider = marketProvider;
    }

    public Market getDefaultMarket() {
        return Iterables.find(marketProvider.get(), Market::isDefaultMarket);
    }

    public Market getMarket(final String code) {
        return marketProvider.get().stream()
                .filter(m -> m.getCodeAsString() != null && m.getCodeAsString().equalsIgnoreCase(code)).findFirst()
                .orElse(null);
    }

    /**
     * FYI: BETA markets will only be returned if it's in one of the desired markets.
     *
     * @param desiredMarketCodes List with market codes prioritized, where first element in iterator is highest priority
     * @return List of markets that are suitable based on input.
     */
    public List<Market> getSuggestedMarkets(List<String> desiredMarketCodes) {
        String suggestedMarketCode = getSuggestedMarket(desiredMarketCodes).getCodeAsString();

        return marketProvider.get().stream()
                .map(m -> {
                    Market market = m.clone();

                    String marketCode = market.getCodeAsString();

                    if (marketCode != null && marketCode.equalsIgnoreCase(suggestedMarketCode)) {
                        market.setSuggested(true);
                    }

                    return market;
                })
                .filter(market -> market.getStatus() == MarketStatus.ENABLED ||
                        (market.getStatus() == MarketStatus.BETA && market.isSuggested()))
                .collect(Collectors.toList());
    }

    private Market getSuggestedMarket(List<String> desiredMarketCodes) {
        for (String suggestedMarketCode : desiredMarketCodes) {
            Market suggestedMarket = getMarket(suggestedMarketCode);

            // When first market is found, let's use that as a suggested market
            if (suggestedMarket != null) {
                return suggestedMarket;
            }
        }

        return getDefaultMarket();
    }
}
