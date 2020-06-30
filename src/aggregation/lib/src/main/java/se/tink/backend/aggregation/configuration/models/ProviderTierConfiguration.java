package se.tink.backend.aggregation.configuration.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderTierConfiguration {
    public enum Tier {
        T1,
        T2,
        T3
    }

    @JsonProperty private Map<String, Map<String, Tier>> providerTierByMarket = new HashMap<>();

    public Optional<Tier> getTierForProvider(String market, String providerName) {
        return Optional.ofNullable(providerTierByMarket.get(market))
                .flatMap(m -> Optional.ofNullable(m.get(providerName)));
    }
}
