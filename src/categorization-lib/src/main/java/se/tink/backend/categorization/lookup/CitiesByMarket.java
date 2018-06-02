package se.tink.backend.categorization.lookup;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Cities;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;

public class CitiesByMarket {
    private static final String CITIES_PATH = "data/seeding/cities-%s.txt";
    private static final LogUtils log = new LogUtils(CitiesByMarket.class);

    private ImmutableMap<Market.Code, Cities> citiesByMarket;

    private CitiesByMarket(Map<Market.Code, Cities> citiesByMarket) {
        this.citiesByMarket = ImmutableMap.copyOf(citiesByMarket);
    }

    public static CitiesByMarket build(Collection<Provider> providers) {
        // Sanity check. Can be removed if we see a use case for an empty collection.
        Preconditions.checkArgument(!providers.isEmpty(), "No providers provided. Is there a use case for this?");

        Map<Market.Code, Cities> citiesByMarket = providers.stream()
                .map(Provider::getMarket)
                .distinct()
                .map(CitiesByMarket::loadCitiesByMarketString)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Function.identity(), CitiesByMarket::loadCitiesByMarket));

        return new CitiesByMarket(citiesByMarket);
    }

    private static Optional<Market.Code> loadCitiesByMarketString(String marketString) {
        try {
            return Optional.of(Market.Code.valueOf(marketString));
        } catch (IllegalArgumentException e) {
            // Thrown by Enum#valueOf.
            log.warn(String.format(
                    "Could not load cities for market '%s' because of Market.Code mapping missing. Continuing anyway (with cities disabled)...",
                    marketString));
            return Optional.empty();
        }
    }

    private static Cities loadCitiesByMarket(Market.Code market) {
        Cities cities = new Cities();
        try {
            cities.loadCities(String.format(CITIES_PATH, market.name().toLowerCase()));
        } catch (IOException e) {
            log.warn(String.format(
                    "Could not load cities for market '%s'. Continuing anyway (with cities disabled)...",
                    market));
        }
        return cities;
    }

    public Optional<Cities> getForMarket(Market.Code market) {
        return Optional.ofNullable(citiesByMarket.get(market));
    }
}

