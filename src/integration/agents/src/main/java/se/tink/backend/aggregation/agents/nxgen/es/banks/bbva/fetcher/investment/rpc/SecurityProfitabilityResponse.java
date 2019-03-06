package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.ProfitabilityEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityProfitabilityResponse {
    private List<ProfitabilityEntity> items;

    @JsonIgnore
    public double getTotalProfit() {
        return Optional.ofNullable(items).orElse(Collections.emptyList()).stream()
                .mapToDouble(ProfitabilityEntity::getTotalProfit)
                .sum();
    }
}
